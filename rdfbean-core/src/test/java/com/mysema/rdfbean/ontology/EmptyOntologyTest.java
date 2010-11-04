/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.ontology;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;


public class EmptyOntologyTest {
    
    @Test
    public void test(){
	Ontology<UID> ontology = EmptyOntology.DEFAULT;
	assertFalse(ontology.getSubproperties(RDF.type).isEmpty());
	assertFalse(ontology.getSubtypes(RDF.Property).isEmpty());
	assertTrue(ontology.getSuperproperties(RDF.type).isEmpty());
	assertTrue(ontology.getSupertypes(RDF.Property).isEmpty());
    }

}
