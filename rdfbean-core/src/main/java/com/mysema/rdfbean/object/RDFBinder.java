/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.List;


/**
 * @author sasa
 *
 */
public interface RDFBinder<R> {

    /**
     * Registers mapping from subject to instance.
     * Binds RDF-mapped properties. 
     * 
     * @param subject
     * @param instance
     */
    public <T> void bind(R subject, T instance);
    
    public List<Object> getConstructorArguments(MappedClass mappedClass, R subject, 
            MappedConstructor mappedConstructor);

}
