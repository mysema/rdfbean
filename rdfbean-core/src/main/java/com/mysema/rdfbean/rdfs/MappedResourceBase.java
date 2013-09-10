/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdfs;

import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;

/**
 * @author sasa
 * 
 */
public class MappedResourceBase {

    @Id
    private ID id;

    public MappedResourceBase() {
    }

    public MappedResourceBase(ID id) {
        this.id = id;
    }

    public ID getId() {
        return id;
    }

    public String toString() {
        return id.toString();
    }
}
