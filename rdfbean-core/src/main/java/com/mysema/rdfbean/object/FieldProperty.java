/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import net.jcip.annotations.Immutable;

import org.apache.commons.collections15.BeanMap;

/**
 * @author sasa
 *
 */
@Immutable
public class FieldProperty extends MappedProperty<Field> {

    private final Field field;

    public FieldProperty(Field field, MappedClass declaringClass) {
        super(field.getName(), field.getAnnotations(), declaringClass);
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public Field getMember() {
        return field;
    }

    @Override
    protected Class<?> getTypeInternal() {
        return field.getType();
    }

    @Override
    public Type getGenericType() {
        return field.getGenericType();
    }

    @Override
    public void setValue(BeanMap beanMap, Object value) {
        if (value == null && field.getType().isPrimitive()) {
            return;
        }
        try {
            field.set(beanMap.getBean(), value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getValue(BeanMap instance) {
        try {
            return field.get(instance.getBean());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isVirtual() {
        return false;
    }
    
}
