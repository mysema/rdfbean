/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.commons.lang.Assert;
import com.mysema.query.annotations.Entity;

/**
 * @author sasa
 *
 */
@Entity
public abstract class ID extends NODE implements Identifier {
    
    private static final long serialVersionUID = 7020057962794085303L;

    final String id;

    ID(String id) {
        this.id = Assert.hasText(id);
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
    
    public static LID localIdRef(String localId) {
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
