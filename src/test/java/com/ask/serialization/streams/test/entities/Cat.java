package com.ask.serialization.streams.test.entities;

import java.util.Objects;

public class Cat extends Animal {
    private final Color color;

    public Cat() {
        super(false, 4);
        this.color = Color.WHITE;
    }

    public Cat(Color color) {
        super(false, 4);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public enum Color {
        WHITE, BLACK, RED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cat cat = (Cat) o;
        return color == cat.color && getName().equals(cat.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, getName());
    }
}
