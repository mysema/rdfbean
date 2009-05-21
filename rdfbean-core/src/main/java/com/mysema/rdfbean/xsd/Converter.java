/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


/**
 * Converter provides Literal to/from Object conversion functionality
 *
 * @author tiwe
 * @version $Id$
 */
public interface Converter<T>{
    
    T fromString(String str);
    
    String toString(T object);
    
}
