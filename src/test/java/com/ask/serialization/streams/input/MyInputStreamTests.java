package com.ask.serialization.streams.input;

import com.ask.serialization.streams.test.entities.Cat;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MyInputStreamTests {

    @Test
    public void readsByte() {
        byte expected = 0x7F;
        byte[] bytes = {expected};
        InputStream is = new MyInputStream(bytes);
        byte actual = is.readByte();
        assertEquals(expected, actual);
    }

    @Test
    public void readsBytes() {
        byte[] expected = {0x7F, 0x00, 0x67};
        InputStream is = new MyInputStream(expected);
        byte[] actual = is.readBytes(3);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void readsBool() {
        byte[] bytes = {0x01};
        InputStream is = new MyInputStream(bytes);
        boolean expected = true;
        boolean actual = is.readBool();
        assertEquals(expected, actual);
    }

    @Test
    public void readsShort() {
        byte[] bytes = {127, -1};
        InputStream is = new MyInputStream(bytes);
        short expected = Short.MAX_VALUE;
        short actual = is.readShort();
        assertEquals(expected, actual);
    }

    @Test
    public void readsChar() {
        byte[] bytes = {-1, -1};
        InputStream is = new MyInputStream(bytes);
        char expected = Character.MAX_VALUE;
        char actual = is.readChar();
        assertEquals(expected, actual);
    }

    @Test
    public void readsInt() {
        byte[] bytes = {127, -1, -1, -1};
        InputStream is = new MyInputStream(bytes);
        int expected = Integer.MAX_VALUE;
        int actual = is.readInt();
        assertEquals(expected, actual);
    }

    @Test
    public void readsFloat() {
        byte[] bytes = {127, 127, -1, -1};
        InputStream is = new MyInputStream(bytes);
        float expected = Float.MAX_VALUE;
        float actual = is.readFloat();
        assertEquals(expected, actual, 0.0f);
    }

    @Test
    public void readsLong() {
        byte[] bytes = {127, -1, -1, -1, -1, -1, -1, -1};
        InputStream is = new MyInputStream(bytes);
        long expected = Long.MAX_VALUE;
        long actual = is.readLong();
        assertEquals(expected, actual);
    }

    @Test
    public void readsDouble() {
        byte[] bytes = {127, -17, -1, -1, -1, -1, -1, -1};
        InputStream is = new MyInputStream(bytes);
        double expected = Double.MAX_VALUE;
        double actual = is.readDouble();
        assertEquals(expected, actual, 0.0d);
    }

    @Test
    public void readsArrayOfPrimitives() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        byte[] bytes = {0x75, 0x49, 0, 0x02, 127, -1, -1, -1, -128, 0, 0, 0};
        InputStream is = new MyInputStream(bytes);
        Object[] expected = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        Object[] actual = is.readArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void readsObject() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // took from MyOutputStreamTests#writesObject
        byte[] bytes = {115, 114, 0, 47, 99, 111, 109, 46, 97, 115, 107, 46, 115, 101, 114, 105, 97, 108, 105, 122, 97, 116, 105, 111, 110, 46, 115, 116, 114, 101, 97, 109, 115, 46, 116, 101, 115, 116, 46, 101, 110, 116, 105, 116, 105, 101, 115, 46, 67, 97, 116, 0, 1, 78, 0, 5, 99, 111, 108, 111, 114, 120, 114, 0, 50, 99, 111, 109, 46, 97, 115, 107, 46, 115, 101, 114, 105, 97, 108, 105, 122, 97, 116, 105, 111, 110, 46, 115, 116, 114, 101, 97, 109, 115, 46, 116, 101, 115, 116, 46, 101, 110, 116, 105, 116, 105, 101, 115, 46, 65, 110, 105, 109, 97, 108, 0, 2, 70, 0, 10, 118, 101, 103, 101, 116, 97, 114, 105, 97, 110, 73, 0, 8, 110, 111, 79, 102, 76, 101, 103, 115, 120, 112, 0, 0, 0, 0, 4, 126, 114, 0, 53, 99, 111, 109, 46, 97, 115, 107, 46, 115, 101, 114, 105, 97, 108, 105, 122, 97, 116, 105, 111, 110, 46, 115, 116, 114, 101, 97, 109, 115, 46, 116, 101, 115, 116, 46, 101, 110, 116, 105, 116, 105, 101, 115, 46, 67, 97, 116, 36, 67, 111, 108, 111, 114, 0, 0, 120, 112, 0, 5, 87, 72, 73, 84, 69};
        InputStream is = new MyInputStream(bytes);
        Object expected = new Cat();
        Object actual = is.readObject();
        assertEquals(expected, actual);
    }

}
