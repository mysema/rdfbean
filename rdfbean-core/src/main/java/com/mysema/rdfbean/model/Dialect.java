/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */ 
package com.mysema.rdfbean.model;

import java.util.Locale;

/**
 * Dialect provides a generic model of RDF APIs
 * 
 * @author Samppa
 * @author Timo
 */
public abstract class Dialect
    <N, 
     R extends N, 
     B extends R, 
     U extends R, 
     L extends N, 
     S> {
    
    public abstract B createBNode();

    public R createResource() {
        return createBNode();
    }

    public abstract S createStatement(R subject, U predicate, N object);
    
    public abstract BID getBID(B bnode);

    public abstract B getBNode(BID bid);

    // Can this be generalized? Jena?
//    public abstract UID getContext(S statement);
    
    public abstract ID getID(R resource);
    
    public abstract LIT getLIT(L literal);

    public abstract L getLiteral(LIT lit);
    
    public abstract L getLiteral(String value);

    public abstract L getLiteral(String value, Locale language);
    
    public abstract L getLiteral(String value, U datatype);
    
    public abstract NODE getNode(N node);

    public abstract NodeType getNodeType(N node); 

    public abstract N getObject(S statement); 
    
    public abstract U getPredicate(S statement);

    public final R getResource(ID id) {
        if (id instanceof UID) {
            return getURI((UID) id);
        } else{
            return getBNode((BID) id);
        }
    }
    
    public abstract R getSubject(S statement);
    
    public abstract UID getUID(U resource);

    public abstract U getURI(UID uid);
    
    public abstract U getURI(String uri);

    public final boolean isResource(N node) {
        NodeType type = getNodeType(node);
        return type == NodeType.URI || type == NodeType.BLANK;
    }
    
}
