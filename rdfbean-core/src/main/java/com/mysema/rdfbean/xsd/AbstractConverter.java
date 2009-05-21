/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

/**
 * 
 * AbstractConverter provides
 *
 * @author tiwe
 * @version $Id$
 *
 * @param <T>
 */
public abstract class AbstractConverter<T> implements Converter<T> {
    @Override
    public final String toString(T object) {
        return object.toString();
    }

}