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
    public final static byte S_CLASSDESC = (byte) 0x72;
    public final static byte S_OBJECT = (byte) 0x73;
    public final static byte S_STRING = (byte) 0x74;
    public final static byte S_ARRAY = (byte) 0x75;
    public final static byte S_NUMBER = (byte) 0x76;
    public final static byte S_ENDBLOCKDATA = (byte) 0x78;
    public final static byte S_ENUM = (byte) 0x7E;
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
