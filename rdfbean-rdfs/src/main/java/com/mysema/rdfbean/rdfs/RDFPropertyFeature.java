/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdfs;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.model.ID;

/**
 * @author sasa
 *
 */
@ClassMapping(ns=OWL.NS)
public enum RDFPropertyFeature {
    AnnotationProperty,
    FunctionalProperty;
    
    @Id
    public ID getId() {
        return ID.uriRef(OWL.NS, this.name());
    }
    
}
