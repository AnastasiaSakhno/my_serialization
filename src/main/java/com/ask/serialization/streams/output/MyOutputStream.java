package com.ask.serialization.streams.output;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.ask.serialization.streams.StreamConstants.*;

public class MyOutputStream implements OutputStream {
    private byte[] buf = new byte[100000];
    private int position = 0;
    private Set<Object> handledObjects = new HashSet<>();

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(buf, position);
    }

    @Override
    public void writeByte(byte obj) {
        buf[position++] = obj;
    }

    @Override
    public void writeBytes(byte[] obj) {
        System.arraycopy(obj, 0, buf, position, obj.length);
        position += obj.length;
    }

    @Override
    public void writeBool(boolean obj) {
        writeByte((byte) (obj ? 1 : 0));
    }

    @Override
    public void writeShort(short obj) {
        writeLongBytes(obj, SHORT_LENGTH);
    }

    @Override
    public void writeChar(char obj) {
        writeLongBytes(obj, CHAR_LENGTH);
    }

    @Override
    public void writeInt(int obj) {
        writeLongBytes(obj, INT_LENGTH);
    }

    @Override
    public void writeFloat(float obj) {
        int i = Float.floatToRawIntBits(obj);
        writeLongBytes(i, FLOAT_LENGTH);
    }

    @Override
    public void writeLong(long obj) {
        writeLongBytes(obj, LONG_LENGTH);
    }

    @Override
    public void writeDouble(double obj) {
        long l = Double.doubleToRawLongBits(obj);
        writeLongBytes(l, DOUBLE_LENGTH);
    }

    private void writeLongBytes(long number, short length) {
        position += length;
        for (int i = position - 1; i >= position - length; i--) {
            buf[i] = (byte) (number & 0xFF);
            number >>= OFFSET;
        }
    }

    @Override
    public void writeArray(Object obj) {
        if (obj == null) {
            writeByte(S_NULL);
            return;
        }

        writeByte(S_ARRAY);

        Class clazz = obj.getClass().getComponentType();
        writeTypeByte(clazz);

        if (clazz.isPrimitive()) {
            if (clazz == Byte.TYPE) {
                byte[] arr = (byte[]) obj;
                writeShort((short) arr.length);
                System.arraycopy(arr, 0, buf, position, arr.length);
                position += arr.length;
            } else if (clazz == Character.TYPE) {
                char[] arr = (char[]) obj;
                writeShort((short) arr.length);
                for (char anArr : arr) {
                    writeChar(anArr);
                }
            } else if (clazz == Short.TYPE) {
                short[] arr = (short[]) obj;
                writeShort((short) arr.length);
                for (short anArr : arr) {
                    writeShort(anArr);
                }
            } else if (clazz == Integer.TYPE) {
                int[] arr = (int[]) obj;
                writeShort((short) arr.length);
                Arrays.stream(arr).forEach(this::writeInt);
            } else if (clazz == Float.TYPE) {
                float[] arr = (float[]) obj;
                writeShort((short) arr.length);
                for (float anArr : arr) {
                    writeFloat(anArr);
                }
            } else if (clazz == Long.TYPE) {
                long[] arr = (long[]) obj;
                writeShort((short) arr.length);
                Arrays.stream(arr).forEach(this::writeLong);
            } else if (clazz == Double.TYPE) {
                Arrays.stream((double[]) obj).forEach(this::writeDouble);
            }
        } else {
            Object[] objs = (Object[]) obj;
            writeShort((short) objs.length);
            Arrays.stream(objs).forEach(this::writeObject);
        }
    }

    @Override
    public void writeObject(Object obj) {
        if (obj == null) {
            writeNull();
            return;
        }

        if (handledObjects.contains(obj)) {
            return;
        }
        handledObjects.add(obj);

        writeByte(S_OBJECT);

        writeClassMetadata(obj.getClass());

        writePrimitivesData(obj);

        writeObjectData(obj, obj.getClass());
    }

    private void writeNull() {
        writeByte(S_NULL);
    }

    private void writeObjectData(Object obj, Class clazz) {
        if(clazz == Object.class) {
            return;
        }

        getFieldsStream(clazz)
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        Object o = field.get(obj);

                        if (field.getType().isArray()) {
                            writeArray(o);
                        } else if (o instanceof Enum) {
                            writeEnum((Enum) o);
                        } else if (o instanceof String) {
                            writeString((String) o);
                        } else if (o instanceof Number) {
                            writeNumber((Number) o);
                        } else if (!field.getType().isPrimitive()) {
                            writeObject(o);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } finally {
                        field.setAccessible(false);
                    }
                });

        writeObjectData(obj, clazz.getSuperclass());
    }

    private void writeString(String obj) {
        writeByte(S_STRING);
        writeInt(obj.length());
        writeBytes(obj.getBytes());
    }

    private void writeNumber(Number obj) {
        writeByte(S_NUMBER);
        if (obj.getClass() == Byte.class) {
            writeByte(SCT_BYTE);
            writeByte((byte) obj);
        } else if (obj.getClass() == Double.class) {
            writeByte(SCT_DOUBLE);
            writeDouble((double) obj);
        } else if (obj.getClass() == Float.class) {
            writeByte(SCT_FLOAT);
            writeFloat((float) obj);
        } else if (obj.getClass() == Integer.class) {
            writeByte(SCT_INT);
            writeInt((int) obj);
        } else if (obj.getClass() == Long.class) {
            writeByte(SCT_LONG);
            writeLong((long) obj);
        } else if (obj.getClass() == Short.class) {
            writeByte(SCT_SHORT);
            writeShort((short) obj);
        } else {
            throw new IllegalStateException("unknown number class");
        }
    }

    private void writeEnum(Enum obj) {
        writeByte(S_ENUM);

        if (handledObjects.contains(obj)) {
            return;
        }
        handledObjects.add(obj);

        writeClassMetadata(obj.getClass());
        writeShort((short) obj.name().length());
        writeBytes(obj.name().getBytes());
    }

    private void writePrimitivesData(Object obj) {
        List<Class> superClasses = getSuperClasses(obj.getClass());
        Collections.reverse(superClasses);

        superClasses.forEach(clazz ->
                writeFieldDataFiltered(
                        field -> field.getType().isPrimitive(),
                        this::writePrimitive,
                        clazz,
                        obj
                ));
    }

    private void writeFieldDataFiltered(
            Function<Field, Boolean> filterFunc,
            Consumer<Object> writeFunc,
            Class clazz,
            Object obj) {
        getFieldsStream(clazz)
                .filter(filterFunc::apply)
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        Object o = field.get(obj);
                        writeFunc.accept(o);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } finally {
                        field.setAccessible(false);
                    }
                });
    }

    private void writePrimitive(Object obj) {
        if (obj instanceof Short) {
            writeShort((Short) obj);
        } else if (obj instanceof Boolean) {
            writeBool((Boolean) obj);
        } else if (obj instanceof Byte) {
            writeByte((Byte) obj);
        } else if (obj instanceof Character) {
            writeChar((Character) obj);
        } else if (obj instanceof Integer) {
            writeInt((Integer) obj);
        } else if (obj instanceof Long) {
            writeLong((Long) obj);
        } else if (obj instanceof Float) {
            writeFloat((Float) obj);
        } else {
            writeDouble((Double) obj);
        }
    }

    private void writeClassMetadata(Class clazz) {
        if (clazz == null || clazz == Object.class || clazz == Enum.class) {
            writeByte(S_NULL);
            return;
        }

        writeByte(S_CLASSDESC);

        writeShort((short) clazz.getName().length());
        writeBytes(clazz.getName().getBytes());

        writeShort((short) getFieldsStream(clazz).count());
        getFieldsStream(clazz)
                .forEach(this::writeFieldMetadata);

//        if (Serializable.class.isAssignableFrom(clazz)) {
//            writeByte(SC_SERIALIZABLE);
//        }

        writeByte(S_ENDBLOCKDATA);
        writeClassMetadata(clazz.getSuperclass());
    }

    private Stream<Field> getFieldsStream(Class clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers())
                        && !Modifier.isTransient(field.getModifiers()));
    }

    private void writeFieldMetadata(Field field) {
        writeTypeByte(field.getType());
        writeShort((short) field.getName().length());
        writeBytes(field.getName().getBytes());
    }

    private void writeTypeByte(Class clazz) {
        if (clazz == Short.TYPE) {
            writeByte(SCT_SHORT);
        } else if (clazz == Boolean.TYPE) {
            writeByte(SCT_BOOL);
        } else if (clazz == Byte.TYPE) {
            writeByte(SCT_BYTE);
        } else if (clazz == Character.TYPE) {
            writeByte(SCT_CHAR);
        } else if (clazz == Integer.TYPE) {
            writeByte(SCT_INT);
        } else if (clazz == Float.TYPE) {
            writeByte(SCT_FLOAT);
        } else if (clazz == Long.TYPE) {
            writeByte(SCT_LONG);
        } else if (clazz == Double.TYPE) {
            writeByte(SCT_DOUBLE);
        } else if (clazz.isEnum()) {
            writeByte(SCT_ENUM);
        } else {
            writeByte(SCT_OBJECT);
        }
    }


    private List<Class> getSuperClasses(Class clazz) {
        List<Class> classes = new ArrayList<>();
        while (clazz != Object.class) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return classes;
    }

}
