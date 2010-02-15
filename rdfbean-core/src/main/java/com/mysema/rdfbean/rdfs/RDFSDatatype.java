/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdfs;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 * 
 */
@ClassMapping(ns = RDFS.NS, ln = "Datatype")
public class RDFSDatatype extends RDFSClass<Object> {

    public RDFSDatatype() {
        super();
    }

    public RDFSDatatype(UID id) {
        super(id);
    }
}
