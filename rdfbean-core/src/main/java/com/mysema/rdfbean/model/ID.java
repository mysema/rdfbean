/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.query.annotations.Entity;
import com.mysema.util.NotEmpty;

/**
 * @author sasa
 *
 */
@Entity
public abstract class ID extends NODE implements Identifier {
    
    private static final long serialVersionUID = 7020057962794085303L;

    protected final String id; // has text, interned for URI

    ID(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public static UID uriRef(String uri) {
        return new UID(uri);
    }
    
    public static UID uriRef(String ns, String ln) {
        return new UID(ns, ln);
    }
    
    public static LID localIdRef(@NotEmpty String localId) {
        return new LID(localId);
    }
    
    public static ID bnodeRef(String bnodeId) {
        return new BID(bnodeId);
    }
    
    public String toString() {
        return id;
    }

    @Override
    public String getValue() {
        return id;
    }

}
