/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.rdfs.RDFProperty;

/**
 * @author sasa
 *
 */
@ClassMapping(ns=OWL.NS)
public class DatatypeProperty extends RDFProperty {

    public DatatypeProperty() {}
	
	public DatatypeProperty(UID id) {
		super(id);
	}

}
