/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.commons.lang.Assert;


/**
 * Local id
 * 
 * @author sasa
 *
 */
public final class LID implements Identifier {

    private static final long serialVersionUID = -5865603861883838644L;

    private final String id;

    public LID(String id) {
        this.id = Assert.hasText(id);
    }

    public LID(long id) {
        this(Long.toString(id, 10));
    }

    public String toString() {
        return "@" + id;
    }
    
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof LID) {
            return this.id.equals(((LID) obj).id);
        } else {
            return false;
        }
    }

}
