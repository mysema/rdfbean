/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

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

    boolean supports(Class<?> cl);

    <T> String toString(T javaValue);
       
}

