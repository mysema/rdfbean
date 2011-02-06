/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.UID;

/**
 * ConverterRegistry is a registry of Converter instances
 *
 * @author tiwe
 * @version $Id$
 */
public interface ConverterRegistry {
    
    <T> T fromString(String value, Class<T> requiredType);

    @Nullable
    UID getDatatype(Class<?> javaClass);
    
    @Nullable
    Class<?> getClass(UID datatype);

    boolean supports(Class<?> cl);

    <T> String toString(T javaValue);
       
}

