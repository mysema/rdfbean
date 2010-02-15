/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import net.jcip.annotations.Immutable;

/**
 * 
 * AbstractConverter provides
 *
 * @author tiwe
 * @version $Id$
 *
 * @param <T>
 */
@Immutable
public abstract class AbstractConverter<T> implements Converter<T> {
    @Override
    public final String toString(T object) {
        return object.toString();
    }

}