package com.ask.serialization.streams.domain;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ClassMetadata {
    private Class clazz;
    private List<FieldMetadata> fieldMetadataList;

    public ClassMetadata(Class clazz, List<FieldMetadata> fields) {
        this.clazz = clazz;
        this.fieldMetadataList = fields;
    }

    public List<FieldMetadata> getFieldMetadataList() {
        return fieldMetadataList;
    }

    public Stream<Field> getFields() {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()));
    }

    public Class getClazz() {
        return clazz;
    }
}
