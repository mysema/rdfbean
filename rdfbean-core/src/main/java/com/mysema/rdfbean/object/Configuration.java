/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.identity.IdentityService;

public interface Configuration {
    
    ConverterRegistry getConverterRegistry();

    boolean allowCreate(Class<?> clazz);
    
    boolean allowRead(MappedPath path);
    
    List<Class<?>> getMappedClasses(UID uid);
    
    Set<Class<?>> getMappedClasses();

    UID getContext(Class<?> javaClass);

    boolean isRestricted(UID uid);

    UID createURI(Object instance);

    IdentityService getIdentityService();

    List<FetchStrategy> getFetchStrategies();
    
}