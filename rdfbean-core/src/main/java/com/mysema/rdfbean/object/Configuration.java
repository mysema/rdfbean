/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.model.UID;

public interface Configuration {

    boolean allowCreate(Class<?> clazz);
    
    boolean allowRead(MappedPath path);
    
    List<Class<?>> getMappedClasses(UID uid);
    
    Set<Class<?>> getMappedClasses();

    UID getContext(Class<?> javaClass);

    @Deprecated
    String getBasePath();

    boolean isRestricted(UID uid);

    UID createURI(Object instance);

}