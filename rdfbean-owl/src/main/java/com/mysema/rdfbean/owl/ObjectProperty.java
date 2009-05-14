/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import java.util.EnumSet;
import java.util.Set;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.rdfs.RDFProperty;

/**
 * @author sasa
 *
 */
@ClassMapping(ns=OWL.NS)
public class ObjectProperty extends RDFProperty {

	@Predicate
    private ObjectProperty inverseOf;

    /**
     * TransitiveProperty, SymmetricProperty or InverseFunctionalProperty
     */
    @Predicate(ns=RDF.NS, ln="type", ignoreInvalid=true)
    private Set<ObjectPropertyFeature> objectPropertyFeatures = EnumSet.noneOf(ObjectPropertyFeature.class);

    public ObjectProperty() {}
    
    public ObjectProperty(UID id) {
		super(id);
	}

    public void addObjectPropertyFeature(ObjectPropertyFeature feature) {
    	objectPropertyFeatures.add(feature);
    }
    
    public ObjectProperty getInverseOf() {
        return inverseOf;
    }

    public Set<ObjectPropertyFeature> getObjectPropertyFeatures() {
        return objectPropertyFeatures;
    }
    
}
