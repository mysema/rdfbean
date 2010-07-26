/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;


public class ClassConverterTest extends AbstractConverterTest<Class<?>>{

    @Override
    Converter<Class<?>> createConverter() {
	return new ClassConverter();
    }

    @Override
    Class<?> createValue() {
	return String.class;
    }
    

}
