/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.schema;

import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.rdfs.RDFSResource;

/**
 * @author sasa
 *
 */
@ClassMapping(ns = RDFS.NS, ln = "Resource")
public class AnyThing extends RDFSResource {

    @Predicate(ns=RDF.NS, ln="type")
    private OWLClass type;
    
    @Predicate(ns=CORE.NS)
    private Integer enumOrdinal;

    public AnyThing(UID id, OWLClass type, Integer enumOrdinal) {
        super(id);
        this.type = type;
        this.enumOrdinal = enumOrdinal;        
    }

    public OWLClass getType() {
        return type;
    }

    public Integer getEnumOrdinal() {
        return enumOrdinal;
    }
    
}
