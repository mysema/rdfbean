/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.springframework.beans.BeanWrapper;

/**
 * @author sasa
 *
 */
public class FieldProperty extends MappedProperty<Field> {

	private Field field;

	public FieldProperty(Field field) {
		super(field.getName(), field.getAnnotations());
		this.field = field;
        this.field.setAccessible(true);
	}

	@Override
	public Field getMember() {
		return field;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public void setValue(BeanWrapper beanWrapper, Object value) {
	    if (value == null && field.getType().isPrimitive()) {
	        return;
	    }
		try {
            field.set(beanWrapper.getWrappedInstance(), value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
	}

    @Override
    public Object getValue(Object instance) {
        try {
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	protected Type getParametrizedType() {
		return field.getGenericType();
	}

    @Override
    public boolean isVirtual() {
        return false;
    }
	
}
