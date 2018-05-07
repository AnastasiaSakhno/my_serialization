package com.ask.serialization.streams.input;

import java.io.IOException;

public interface InputStream {

    byte readByte();

    byte[] readBytes(int length);

    boolean readBool();

    short readShort();

    char readChar();

    int readInt();

    float readFloat();

    long readLong();

    double readDouble();

    Object readObject() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException;
}
