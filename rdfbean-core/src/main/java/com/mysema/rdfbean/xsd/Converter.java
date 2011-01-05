/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import net.jcip.annotations.Immutable;


/**
 * Converter provides Literal to/from Object conversion functionality
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public interface Converter<T>{
    
    Class<T> getJavaType();
    
    T fromString(String str);
    
    String toString(T object);
    
}
