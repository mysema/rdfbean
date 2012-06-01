/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
public enum ContainerType {
    /**
     * 
     */
    NONE(null),
    /**
     * 
     */
    LIST(RDF.List), 
    /**
     * 
     */
    SEQ(RDF.Seq), 
    /**
     * 
     */
    BAG(RDF.Bag), 
    /**
     * 
     */
    ALT(RDF.Alt);

    private final UID uid;
    
    private ContainerType(UID uid) {
        this.uid = uid;
    }

    public UID getUID() {
        return uid;
    }

}
