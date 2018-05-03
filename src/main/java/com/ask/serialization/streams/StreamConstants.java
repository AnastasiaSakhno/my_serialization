package com.ask.serialization.streams;

public class StreamConstants {

    public static final short SHORT_LENGTH = 2;
    public static final short INT_LENGTH = 4;
    public static final short LONG_LENGTH = 8;
    public static final short FLOAT_LENGTH = 4;
    public static final short DOUBLE_LENGTH = 8;
    public static final short CHAR_LENGTH = 2;

    public static final short OFFSET = 8;

    // TODO my values
    public final static byte S_NULL = (byte) 0x70;
    public final static byte S_REFERENCE = (byte) 0x71;
    public final static byte S_CLASSDESC = (byte) 0x72;
    public final static byte S_OBJECT = (byte) 0x73;
    public final static byte S_STRING = (byte) 0x74;
    public final static byte S_ARRAY = (byte) 0x75;
    public final static byte S_CLASS = (byte) 0x76;
    public final static byte S_BLOCKDATA = (byte) 0x77;
    public final static byte S_ENDBLOCKDATA = (byte) 0x78;
    public final static byte S_RESET = (byte) 0x79;
    public final static byte S_BLOCKDATALONG = (byte) 0x7A;
    public final static byte S_EXCEPTION = (byte) 0x7B;
    public final static byte S_LONGSTRING = (byte) 0x7C;
    public final static byte S_PROXYCLASSDESC = (byte) 0x7D;
    public final static byte S_ENUM = (byte) 0x7E;
    public final static byte S_MAX = (byte) 0x7E;
    public final static byte SC_BLOCK_DATA = 0x08;
    public final static byte SC_SERIALIZABLE = 0x02;
    public final static byte SC_EXTERNALIZABLE = 0x04;
    public final static byte SC_ENUM = 0x10;

    public final static byte SCT_BYTE = 0x45;
    public final static byte SCT_BOOL = 0x46;
    public final static byte SCT_SHORT = 0x47;
    public final static byte SCT_CHAR = 0x48;
    public final static byte SCT_INT = 0x49;
    public final static byte SCT_FLOAT = 0x4A;
    public final static byte SCT_LONG = 0x4B;
    public final static byte SCT_DOUBLE = 0x4C;
    public final static byte SCT_OBJECT = 0x4D;
    public final static byte SCT_ENUM = 0x4E;
}
