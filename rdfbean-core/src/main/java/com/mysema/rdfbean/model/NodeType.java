/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */ 
package com.mysema.rdfbean.model;

/**
 * NodeType defines the three node types of RDF : blank nodes, URIs and literals
 * 
 * @author Timo
 */
public enum NodeType {
    /**
     * blank node
     */
    BLANK, 
    /**
     * URI
     */
    URI, 
    /**
     * literal
     */
    LITERAL
}
