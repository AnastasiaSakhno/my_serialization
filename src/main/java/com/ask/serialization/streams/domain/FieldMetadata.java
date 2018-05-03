package com.ask.serialization.streams.domain;

public class FieldMetadata {
    private Class type;
    private String name;

    public FieldMetadata(Class type, String name) {
        this.type = type;
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
