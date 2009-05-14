/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import org.springframework.beans.BeanWrapper;

import com.mysema.rdfbean.annotations.InjectProperty;

/**
 * @author sasa
 *
 */
public class ConstructorParameter extends MappedProperty<Constructor<?>> {

	private Constructor<?> constructor;

	private int parameterIndex;
	
	ConstructorParameter(Constructor<?> constructor, int parameterIndex) {
		super(null, constructor.getParameterAnnotations()[parameterIndex]);
		this.constructor = constructor;
		this.parameterIndex = parameterIndex;
	}
	
	@Override
	protected Constructor<?> getMember() {
		return constructor;
	}
	
	@Override
	protected Type getParametrizedType() {
		return constructor.getGenericParameterTypes()[parameterIndex];
	}
	
	public String getReferencedProperty() {
		return getAnnotation(InjectProperty.class).value();
	}

	@Override
	public Class<?> getType() {
		return constructor.getParameterTypes()[parameterIndex];
	}

	@Override
	public Object getValue(Object instance) {
		throw new UnsupportedOperationException();
	}

	public boolean isPropertyReference() {
		return isAnnotationPresent(InjectProperty.class);
	}

	@Override
	public void setValue(BeanWrapper beanWrapper, Object value) {
		throw new UnsupportedOperationException();
	}

    @Override
    public boolean isVirtual() {
        return false;
    }
	
}
