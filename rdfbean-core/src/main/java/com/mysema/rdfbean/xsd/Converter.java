/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.model.UID;


/**
 * Converter provides Literal to/from Object conversion functionality
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public interface Converter<T>{
    
    UID getType();
    
    Class<T> getJavaType();
    
    T fromString(String str);
    
    String toString(T object);
    
}
