package com.ask.serialization.streams.input;

import com.ask.serialization.streams.domain.ClassMetadata;
import com.ask.serialization.streams.domain.FieldMetadata;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ask.serialization.streams.StreamConstants.*;

public class MyInputStream implements InputStream {
    private final byte[] buf;
    private int position = 0;

    public MyInputStream(byte[] buf) {
        this.buf = buf;
    }

    @Override
    public byte readByte() {
        return buf[position++];
    }

    @Override
    public byte[] readBytes(int length) {
        byte[] arr = new byte[length];
        System.arraycopy(buf, position, arr, 0, length);
        position += length;
        return arr;
    }

    @Override
    public boolean readBool() {
        return readByte() != 0;
    }

    @Override
    public short readShort() {
        return (short) readNumber(SHORT_LENGTH);
    }

    @Override
    public char readChar() {
        return (char) readNumber(CHAR_LENGTH);
    }

    @Override
    public int readInt() {
        return (int) readNumber(INT_LENGTH);
    }

    @Override
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public long readLong() {
        return readNumber(LONG_LENGTH);
    }

    private long readNumber(short length) {
        long value = 0;
        position += length;
        for (int i = 1; i <= length; i++) {
            value += (buf[position - i] & 0xFFL) << (i - 1) * OFFSET;
        }
        return value;
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    private Object readArray() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        byte b = readByte();

        short length = readShort();

        switch (b) {
            case SCT_BOOL:
                boolean[] booleans = new boolean[length];
                for (int i = 0; i < length; i++) {
                    booleans[i] = readBool();
                }
                return booleans;
            case SCT_BYTE:
                return readBytes(length);
            case SCT_CHAR:
                char[] chars = new char[length];
                for (int i = 0; i < length; i++) {
                    chars[i] = readChar();
                }
                return chars;
            case SCT_DOUBLE:
                double[] doubles = new double[length];
                for (int i = 0; i < length; i++) {
                    doubles[i] = readDouble();
                }
                return doubles;
            case SCT_FLOAT:
                float[] floats = new float[length];
                for (int i = 0; i < length; i++) {
                    floats[i] = readFloat();
                }
                return floats;
            case SCT_INT:
                int[] ints = new int[length];
                for (int i = 0; i < length; i++) {
                    ints[i] = readInt();
                }
                return ints;
            case SCT_LONG:
                long[] longs = new long[length];
                for (int i = 0; i < length; i++) {
                    longs[i] = readLong();
                }
                return longs;
            case SCT_SHORT:
                short[] shorts = new short[length];
                for (int i = 0; i < length; i++) {
                    shorts[i] = readShort();
                }
                return shorts;
            case SCT_OBJECT:
                Object[] objects = new Object[length];
                for (int i = 0; i < length; i++) {
                    objects[i] = readObject();
                }
                return objects;
            default:
                throw new IllegalStateException("could not find array's type");
        }
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        byte b = readByte();

        if (b == S_NULL) {
            return null;
        } else if (b == S_ARRAY) {
            return readArray();
        } else if (b == S_STRING) {
            return readString();
        } else if (b == S_NUMBER) {
            return readNumber();
        } else if (b == S_OBJECT) {
            List<ClassMetadata> classMetadataList = readClassMetadata();
            Object obj = createInstance(classMetadataList);
            readObjectData(obj, classMetadataList);
            return obj;
        } else {
            throw new IOException("unknown object descriptor");
        }
    }

    private List<ClassMetadata> readClassMetadata() throws IOException, ClassNotFoundException {
        List<ClassMetadata> superClasses = new ArrayList<>();
        readClassMetadata(superClasses);
        return superClasses;
    }

    private void readClassMetadata(List<ClassMetadata> superClasses) throws IOException, ClassNotFoundException {
        byte b = readByte();

        switch (b) {
            case S_NULL:
                return;
            case S_CLASSDESC:
                short classNameLength = readShort();
                String className = new String(readBytes(classNameLength));
                Class clazz = Class.forName(className);
                short fieldsLength = readShort();
                List<FieldMetadata> fields = readFieldsMetadata(fieldsLength);
                superClasses.add(new ClassMetadata(clazz, fields));
                break;
            case S_ENDBLOCKDATA:
                break;
            default:
                throw new IOException("unknown class metadata descriptor");
        }
        readClassMetadata(superClasses);
    }

    private List<FieldMetadata> readFieldsMetadata(int length) {
        List<FieldMetadata> fields = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            fields.add(readFieldMetadata());
        }
        return fields;
    }

    private FieldMetadata readFieldMetadata() {
        byte type = readByte();
        short nameLength = readShort();
        String name = new String(readBytes(nameLength));

        return new FieldMetadata(getFieldType(type), name);
    }

    private Class getFieldType(byte b) {
        if (b == SCT_BOOL) {
            return Boolean.TYPE;
        } else if (b == SCT_BYTE) {
            return Byte.TYPE;
        } else if (b == SCT_CHAR) {
            return Character.TYPE;
        } else if (b == SCT_DOUBLE) {
            return Double.TYPE;
        } else if (b == SCT_FLOAT) {
            return Float.TYPE;
        } else if (b == SCT_INT) {
            return Integer.TYPE;
        } else if (b == SCT_LONG) {
            return Long.TYPE;
        } else if (b == SCT_SHORT) {
            return Short.TYPE;
        } else if (b == SCT_ENUM) {
            return Enum.class;
        } else if (b == SCT_OBJECT) {
            return Object.class;
        } else {
            throw new IllegalStateException("could not find field's type");
        }
    }

    private Object createInstance(List<ClassMetadata> superClasses) throws IOException, IllegalAccessException, InstantiationException {
        Object obj = null;
        for (ClassMetadata cm : superClasses) {
            boolean hasDefaultConstructor = Arrays.stream(cm.getClazz().getConstructors())
                    .anyMatch(c -> c.getParameterCount() == 0);
            if (hasDefaultConstructor && Serializable.class.isAssignableFrom(cm.getClazz())) {
                obj = cm.getClazz().newInstance();
                break;
            }
        }

        if (obj == null) {
            throw new IOException("could not find serializable default constructor");
        }
        return obj;
    }

    private void readObjectData(Object obj, List<ClassMetadata> classMetadataList) {
        for (ClassMetadata cm : classMetadataList) {
            cm.getFields()
                    .filter(field -> field.getType().isPrimitive())
                    .forEach(field -> {
                        field.setAccessible(true);
                        try {
                            readPrimitiveData(field, obj);
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        } finally {
                            field.setAccessible(false);
                        }
                    });
        }

        for (ClassMetadata cm : classMetadataList) {
            cm.getFields()
                    .forEach(field -> {
                        field.setAccessible(true);
                        try {
                            if (field.getType().isEnum()) {
                                readEnumData(field, obj);
                            } else if (field.getType().isArray()) {
                                field.set(obj, readArray());
                            } else if (!field.getType().isPrimitive()) {
                                field.set(obj, readObject());
                            }
                        } catch (ReflectiveOperationException | IOException e) {
                            e.printStackTrace();
                        } finally {
                            field.setAccessible(false);
                        }
                    });
        }
    }

    private void readPrimitiveData(Field field, Object obj) throws IllegalAccessException {
        if (field.getType() == Short.TYPE) {
            field.set(obj, readShort());
        } else if (field.getType() == Boolean.TYPE) {
            field.set(obj, readBool());
        } else if (field.getType() == Byte.TYPE) {
            field.set(obj, readByte());
        } else if (field.getType() == Character.TYPE) {
            field.set(obj, readChar());
        } else if (field.getType() == Integer.TYPE) {
            field.set(obj, readInt());
        } else if (field.getType() == Float.TYPE) {
            field.set(obj, readFloat());
        } else if (field.getType() == Long.TYPE) {
            field.set(obj, readLong());
        } else if (field.getType() == Double.TYPE) {
            field.set(obj, readDouble());
        } else {
            // should never happen
            throw new IllegalStateException("unknown primitive type");
        }
    }

    private String readString() {
        int length = readInt();
        return new String(readBytes(length));
    }

    private Number readNumber() {
        byte type = readByte();

        if (type == SCT_BYTE) {
            return readByte();
        } else if (type == SCT_DOUBLE) {
            return readDouble();
        } else if (type == SCT_FLOAT) {
            return readFloat();
        } else if (type == SCT_INT) {
            return readInt();
        } else if (type == SCT_LONG) {
            return readLong();
        } else if (type == SCT_SHORT) {
            return readShort();
        } else {
            throw new IllegalStateException("wrong number descriptor");
        }
    }

    private void readEnumData(Field field, Object obj) throws IllegalAccessException, IOException, ClassNotFoundException {
        byte b = readByte();
        if (b == SC_ENUM) {
            throw new IllegalStateException("wrong enum descriptor");
        }

        readClassMetadata();
        short nameLength = readShort();
        String name = new String(readBytes(nameLength));
        Enum o = Enum.valueOf((Class<Enum>) field.getType(), name);
        field.set(obj, o);
    }
}
