package com.ask.serialization.streams.output;

import com.ask.serialization.streams.test.entities.Animal;
import com.ask.serialization.streams.test.entities.Cat;
import com.sun.deploy.util.StringUtils;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MyOutputStreamTests {

    @Test
    public void writesByte() {
        byte n = Byte.MAX_VALUE;
        String expected = "7F";
        OutputStream os = new MyOutputStream();
        os.writeByte(n);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesBytes() {
        byte[] bytes = {Byte.MAX_VALUE, Byte.MIN_VALUE};
        String expected = "7F80";
        OutputStream os = new MyOutputStream();
        os.writeBytes(bytes);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesBool() {
        String expected = "0100";
        OutputStream os = new MyOutputStream();
        os.writeBool(true);
        os.writeBool(false);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesShort() {
        short n = Short.MAX_VALUE;
        String expected = "7FFF";
        OutputStream os = new MyOutputStream();
        os.writeShort(n);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesChar() {
        char n = Character.MAX_VALUE;
        String expected = "FFFF";
        OutputStream os = new MyOutputStream();
        os.writeChar(n);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesInt() {
        int n = Integer.MAX_VALUE;
        String expected = "7FFFFFFF";
        OutputStream os = new MyOutputStream();
        os.writeInt(n);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesFloat() {
        float n = Float.MAX_VALUE;
        String expected = "7F7FFFFF";
        OutputStream os = new MyOutputStream();
        os.writeFloat(n);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesLong() {
        long n = Long.MAX_VALUE;
        String expected = "7FFFFFFFFFFFFFFF";
        OutputStream os = new MyOutputStream();
        os.writeLong(n);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesDouble() {
        double n = Double.MAX_VALUE;
        String expected = "7FEFFFFFFFFFFFFF";
        OutputStream os = new MyOutputStream();
        os.writeDouble(n);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesArrayOfPrimitives() {
        int[] arr = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        String expected = "754900027FFFFFFF80000000";
        OutputStream os = new MyOutputStream();
        os.writeArray(arr);
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    @Test
    public void writesObject() {
        Cat obj = new Cat();
        String catClassName = Cat.class.getName();
        String animalClassName = Animal.class.getName();
        String colorEnumName = Cat.Color.class.getName();
        String catColor = obj.getColor().name();
        String expected = "73" + "72" + pob((short) catClassName.length()) + pob(catClassName)
                + "0001"
                + "4E0005" + pob("color")
                + "78"
                + "72" + pob((short) animalClassName.length()) + pob(animalClassName)
                + "0002"
                + "46000A" + pob("vegetarian")
                + "490008" + pob("noOfLegs")
                + "78"
                + "70" + "00" + "00000004"
                + "7E" + "72" + pob((short) colorEnumName.length()) + pob(colorEnumName)
                + "0000"
                + "78"
                + "70"
                + pob((short) catColor.length()) + pob(catColor)
                ;
        OutputStream os = new MyOutputStream();
        os.writeObject(obj);
//        System.out.println(Arrays.toString(os.getBytes()));
        String actual = DatatypeConverter.printHexBinary(os.getBytes());
        assertEquals(expected, actual);
    }

    private String pob(short obj) {
        OutputStream os = new MyOutputStream();
        os.writeShort(obj);
        return DatatypeConverter.printHexBinary(os.getBytes());
    }

    private String pob(String str) {
        OutputStream os = new MyOutputStream();
        os.writeBytes(str.getBytes());
        return DatatypeConverter.printHexBinary(os.getBytes());
    }
}
