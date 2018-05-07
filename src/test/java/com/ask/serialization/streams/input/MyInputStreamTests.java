package com.ask.serialization.streams.input;

import com.ask.serialization.streams.output.MyOutputStream;
import com.ask.serialization.streams.output.OutputStream;
import com.ask.serialization.streams.test.entities.Animal;
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
        boolean actual = is.readBool();
        assertTrue(actual);
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
        int[] expected = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] actual = (int[]) is.readArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void readsObject() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Animal cat = new Cat();
        cat.setName("Tom");
        ((Cat) cat).setColor(Cat.Color.BLACK);
        OutputStream os = new MyOutputStream();
        os.writeObject(cat);
        byte[] bytes = os.getBytes();
        InputStream is = new MyInputStream(bytes);
        Object actual = is.readObject();
        assertEquals(cat, actual);
    }

}
