/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */ 
package com.mysema.rdfbean.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Dialect provides a generic service for RDF node creation and conversion
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

    private static final Map<String,UID> datatypeUIDCache = new HashMap<String,UID>();
    
    static{
        for (UID uid : XSD.ALL){
            datatypeUIDCache.put(uid.getId(), uid);
        }
    }
    
    public abstract B createBNode();
    
    public abstract S createStatement(R subject, U predicate, N object);
    
    public abstract S createStatement(R subject, U predicate, N object, @Nullable U context);

    public abstract BID getBID(B bnode);
    
    public abstract B getBNode(BID bid);
    
    protected UID getDatatypeUID(String datatype){
        UID uid = datatypeUIDCache.get(datatype);
        if (uid == null){
            uid = new UID(datatype);
            datatypeUIDCache.put(datatype, uid);            
        }
        return uid;
    }

    public abstract ID getID(R resource);
    
    public abstract LIT getLIT(L literal);
    
    public abstract L getLiteral(LIT lit);

    public N getNode(NODE node){
        if (node.isLiteral()){
            return getLiteral((LIT)node);
        }else if (node.isBNode()){
            return getBNode((BID)node);
        }else{
            return getURI((UID)node);
        }
    }
    
    public abstract NODE getNODE(N node);
    
    public abstract NodeType getNodeType(N node); 

    public abstract N getObject(S statement); 
    
    public abstract U getPredicate(S statement);

    public final R getResource(ID id) {
        if (id.isURI()){
            return getURI((UID)id);
        }else{
            return getBNode((BID)id);
        }        
    }
        
    public abstract R getSubject(S statement);
    
    public abstract UID getUID(U resource);
    
    public abstract U getURI(UID uid);
    
}
