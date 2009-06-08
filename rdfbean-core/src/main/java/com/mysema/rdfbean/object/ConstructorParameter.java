/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import org.apache.commons.collections15.BeanMap;

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
		init();
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
	public Class<?> getTypeInternal() {
		return constructor.getParameterTypes()[parameterIndex];
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
