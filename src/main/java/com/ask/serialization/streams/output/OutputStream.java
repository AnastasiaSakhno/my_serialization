package com.ask.serialization.streams.output;

public interface OutputStream {

    byte[] getBytes();

    void writeByte(byte obj);

    void writeBytes(byte[] obj);

    void writeBool(boolean obj);

    void writeShort(short obj);

    void writeChar(char obj);

    void writeInt(int obj);

    void writeFloat(float obj);

    void writeLong(long obj);

    void writeDouble(double obj);

    void writeArray(Object obj);

    void writeObject(Object obj);
}

