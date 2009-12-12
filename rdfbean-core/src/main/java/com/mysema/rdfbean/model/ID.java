/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import net.jcip.annotations.Immutable;

import com.mysema.query.annotations.QueryEntity;

/**
 * ID represents a general RDF resource
 * 
 * @author sasa
 *
 */
@QueryEntity
@Immutable
public abstract class ID extends NODE implements Identifier {
    
    private static final long serialVersionUID = 7020057962794085303L;

    protected final String id; // has text, interned for URI

    ID(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public String toString() {
        return id;
    }

    @Override
    public String getValue() {
        return id;
    }

}
