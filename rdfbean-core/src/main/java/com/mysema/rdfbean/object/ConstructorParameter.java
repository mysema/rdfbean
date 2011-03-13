/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import org.apache.commons.collections15.BeanMap;

/**
 * @author sasa
 * 
 */
@Immutable
public final class ConstructorParameter extends MappedProperty<Constructor<?>> {

    private final Constructor<?> constructor;

    private final int parameterIndex;

    @Nullable
    private final String property;
    
    public ConstructorParameter(Constructor<?> constructor, int parameterIndex, MappedClass declaringClass, @Nullable String property) {
        super(null, constructor.getParameterAnnotations()[parameterIndex], declaringClass);
        this.constructor = constructor;
        this.parameterIndex = parameterIndex;
        this.property = property;
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
        return property != null;
    }

    @Override
    public void setValue(BeanMap beanWrapper, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVirtual() {
        return property == null;
    }

}
