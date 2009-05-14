/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.UID;

public interface Configuration {

    boolean allowCreate(Class<?> clazz);
    
    boolean allowRead(MappedPath path);

    <T, N, R extends N, U extends R> T createInstance(R subject, 
            Collection<R> types, 
            Class<T> requiredType,
            RDFBinder<R> binder, 
            Dialect<N, R, ?, U, ?, ?> dialect);
    
    List<Class<?>> getMappedClasses(UID uid);
    
    Set<Class<?>> getMappedClasses();

    UID getContext(Class<?> javaClass);

    @Deprecated
    String getBasePath();

    boolean isRestricted(UID uid);

    UID createURI(Object instance);

}