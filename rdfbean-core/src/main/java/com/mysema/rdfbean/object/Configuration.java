/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.identity.IdentityService;

/**
 * Configuration defines the main configuration interface for RDFBean
 *
 * @author tiwe
 * @version $Id$
 *
 */
public interface Configuration {
    
    ConverterRegistry getConverterRegistry();

    boolean allowCreate(Class<?> clazz);
    
    boolean allowRead(MappedPath path);
    
    List<Class<?>> getMappedClasses(UID uid);
    
    Set<Class<?>> getMappedClasses();

    UID getContext(Class<?> javaClass, @Nullable ID subject);

    boolean isRestricted(UID uid);

    UID createURI(Object instance);

    IdentityService getIdentityService();

    List<FetchStrategy> getFetchStrategies();
    
}