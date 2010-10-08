/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.model.RDF;

/**
 * NamespacesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class NamespacesTest {

    @Test
    public void GetPrefix() {
        assertEquals("rdf", Namespaces.getPrefix(RDF.NS));
    }

    @Test
    public void GetReadableURI() {
        assertEquals("rdf:type", Namespaces.getReadableURI(RDF.NS,"type"));
        assertEquals("rdf:", Namespaces.getReadableURI(RDF.NS,null));
        assertEquals("<urn:test>", Namespaces.getReadableURI("urn:", "test"));
        assertEquals("test", Namespaces.getReadableURI(null, "test"));        
    }
    
    @Test
    public void Register(){
	Namespaces.register("test", TEST.NS);
        assertEquals("test:test", Namespaces.getReadableURI(TEST.NS, "test"));	
    }

}
