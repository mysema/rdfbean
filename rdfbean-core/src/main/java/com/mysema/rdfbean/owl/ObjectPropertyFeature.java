/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 * 
 */
@ClassMapping(ns = OWL.NS)
public enum ObjectPropertyFeature {
    InverseFunctionalProperty, 
    SymmetricProperty, 
    TransitiveProperty;

    @Id
    public UID getId() {
        return new UID(OWL.NS, this.name());
    }

}
