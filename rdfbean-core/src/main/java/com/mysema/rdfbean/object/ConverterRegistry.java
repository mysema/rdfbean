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
    
    /**
     * @param <T>
     * @param value
     * @param requiredType
     * @return
     */
    <T> T fromString(String value, Class<T> requiredType);

    /**
     * @param javaClass
     * @return
     */
    @Nullable
    UID getDatatype(Class<?> javaClass);

    /**
     * @param cl
     * @return
     */
    boolean supports(Class<?> cl);

    /**
     * @param <T>
     * @param javaValue
     * @return
     */
    <T> String toString(T javaValue);
       
}

