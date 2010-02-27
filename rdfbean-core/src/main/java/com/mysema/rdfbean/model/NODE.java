/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * NODE represents a general RDF node
 * 
 * @author sasa
 *
 */
@Immutable
public abstract class NODE implements Serializable {

    private static final long serialVersionUID = -6921484648846884179L;

    // This is closed api
    NODE() {}
    
    public abstract String getValue();
    
    public abstract NodeType getNodeType();

    public abstract boolean isResource();
    
    public abstract boolean isURI();
    
    public abstract boolean isBNode();

    public abstract boolean isLiteral();
    
    public LIT asLiteral(){
        throw new UnsupportedOperationException();
    }
    
    public ID asResource(){
        throw new UnsupportedOperationException();
    }
    
    public UID asURI(){
        throw new UnsupportedOperationException();
    }
    
    public BID asBNode(){
        throw new UnsupportedOperationException();
    }
}
