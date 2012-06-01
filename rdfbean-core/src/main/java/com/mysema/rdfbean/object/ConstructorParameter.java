/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import javax.annotation.concurrent.Immutable;

import com.mysema.util.BeanMap;

/**
 * @author sasa
 * 
 */
@Immutable
public final class ConstructorParameter extends MappedProperty<Constructor<?>> {

    private final Constructor<?> constructor;

    private final int parameterIndex;

    private final String property;
    
    private final boolean reference;
    
    public ConstructorParameter(Constructor<?> constructor, int parameterIndex, MappedClass declaringClass, String property, boolean reference) {
        super(property, constructor.getParameterAnnotations()[parameterIndex], declaringClass);
        this.constructor = constructor;
        this.parameterIndex = parameterIndex;
        this.property = property;
        this.reference = reference;
    }

    @Override
    protected Constructor<?> getMember() {
        return constructor;
    }

    public String getReferencedProperty() {
        return property;
    }

    @Override
    protected Class<?> getTypeInternal() {
        return constructor.getParameterTypes()[parameterIndex];
    }

    @Override
    public Type getGenericType() {
        return constructor.getGenericParameterTypes()[parameterIndex];
    }

    @Override
    public Object getValue(BeanMap instance) {
        throw new UnsupportedOperationException();
    }

    public boolean isPropertyReference() {
        return reference;
    }

    @Override
    public void setValue(BeanMap beanWrapper, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVirtual() {
        return !reference;
    }

}
