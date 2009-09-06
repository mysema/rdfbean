/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.schema;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.rdfs.RDFSResource;

/**
 * @author sasa
 *
 */
@ClassMapping
public class AnyThing extends RDFSResource {

    @Predicate(ns=RDF.NS, ln="type")
    private OWLClass type;

    public AnyThing(UID id, OWLClass type) {
        super(id);
        this.type = type;
    }

    public OWLClass getType() {
        return type;
    }
    
}
