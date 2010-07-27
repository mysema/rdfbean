/*
 * Copyright (c) 2010 Mysema Ltd.
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
import com.mysema.rdfbean.xsd.ConverterRegistry;

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
    
    boolean isMapped(Class<?> javaClass);
    
    MappedClass getMappedClass(Class<?> javaClass);
    
    List<MappedClass> getMappedClasses(UID uid);
    
    Set<MappedClass> getMappedClasses();

    @Nullable
    UID getContext(Class<?> javaClass, @Nullable ID subject);

    boolean isRestricted(UID uid);

    @Nullable
    UID createURI(Object instance);

    List<FetchStrategy> getFetchStrategies();
        
}