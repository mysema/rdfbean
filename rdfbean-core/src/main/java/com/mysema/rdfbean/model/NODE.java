/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

import com.mysema.query.annotations.QueryEntity;

/**
 * NODE represents a general RDF node
 * 
 * @author sasa
 *
 */
@QueryEntity
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
    
}
