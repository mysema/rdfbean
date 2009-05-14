/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.rdfs.RDFSResource;

/**
 * @author sasa
 *
 */
@ClassMapping(ns=OWL.NS)
public class Thing extends RDFSResource {

    public Thing() {
        super();
    }

    public Thing(ID id) {
        super(id);
    }

}
