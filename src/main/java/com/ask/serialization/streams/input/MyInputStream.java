package com.ask.serialization.streams.input;

import com.ask.serialization.streams.domain.ClassMetadata;
import com.ask.serialization.streams.domain.FieldMetadata;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

    @Override
    public Object[] readArray() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        byte b = readByte();
        if (b != S_ARRAY) {
            throw new IllegalStateException("could not read array");
        }

        b = readByte();

        short length = readShort();
        Object[] arr = new Object[length];

        for (int i = 0; i < length; i++) {
            Object read;
            switch (b) {
                case SCT_BOOL:
                    read = readBool();
                    break;
                case SCT_BYTE:
                    read = readByte();
                    break;
                case SCT_CHAR:
                    read = readChar();
                    break;
                case SCT_DOUBLE:
                    read = readDouble();
                    break;
                case SCT_FLOAT:
                    read = readFloat();
                    break;
                case SCT_INT:
                    read = readInt();
                    break;
                case SCT_LONG:
                    read = readLong();
                    break;
                case SCT_SHORT:
                    read = readShort();
                    break;
                case SCT_OBJECT:
                    read = readObject();
                    break;
                default:
                    throw new IllegalStateException("could not find array's type");
            }
            arr[i] = read;
        }
        return arr;
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        byte b = readByte();

        switch (b) {
            case S_NULL:
                return null;
            case S_OBJECT:
                List<ClassMetadata> classMetadataList = readClassMetadata();
                Object obj = createInstance(classMetadataList);
                readObjectData(obj, classMetadataList);
                return obj;
            default:
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
        switch (b) {
            case SCT_BOOL:
                return Boolean.TYPE;
            case SCT_BYTE:
                return Byte.TYPE;
            case SCT_CHAR:
                return Character.TYPE;
            case SCT_DOUBLE:
                return Double.TYPE;
            case SCT_FLOAT:
                return Float.TYPE;
            case SCT_INT:
                return Integer.TYPE;
            case SCT_LONG:
                return Long.TYPE;
            case SCT_SHORT:
                return Short.TYPE;
            case SCT_ENUM:
                return Enum.class;
            case SCT_OBJECT:
                return Object.class;
            default:
                throw new IllegalStateException("could not find field's type");
        }
    }

    private Object createInstance(List<ClassMetadata> superClasses) throws IOException, IllegalAccessException, InstantiationException {
        Object obj = null;
        for (ClassMetadata cm : superClasses) {
            if (Serializable.class.isAssignableFrom(cm.getClazz())) {
                obj = cm.getClazz().newInstance();
                break;
            }
        }
        if (obj == null) {
            throw new IOException("could not find not serializable super classes default constructor");
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
                            } else {
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

    private Enum readEnumData(Field field, Object obj) {
        throw new NotImplementedException();
    }
}
