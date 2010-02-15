/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import net.jcip.annotations.Immutable;

import org.apache.commons.collections15.BeanMap;

import com.mysema.rdfbean.annotations.InjectProperty;

/**
 * @author sasa
 * 
 */
@Immutable
public class ConstructorParameter extends MappedProperty<Constructor<?>> {

    private final Constructor<?> constructor;

    private final int parameterIndex;

    ConstructorParameter(Constructor<?> constructor, int parameterIndex,
            MappedClass declaringClass) {
        super(null, constructor.getParameterAnnotations()[parameterIndex],
                declaringClass);
        this.constructor = constructor;
        this.parameterIndex = parameterIndex;
    }

    @Override
    protected Constructor<?> getMember() {
        return constructor;
    }

    public String getReferencedProperty() {
        return getAnnotation(InjectProperty.class).value();
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
        return isAnnotationPresent(InjectProperty.class);
    }

    @Override
    public void setValue(BeanMap beanWrapper, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVirtual() {
        return getAnnotation(InjectProperty.class) == null;
    }

}
