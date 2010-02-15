/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdfs;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.owl.OWL;

/**
 * @author sasa
 * 
 */
@ClassMapping(ns = OWL.NS)
public enum RDFPropertyFeature {
    AnnotationProperty, 
    FunctionalProperty;

    @Id
    public ID getId() {
        return new UID(OWL.NS, this.name());
    }

}
