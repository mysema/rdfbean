package com.mysema.rdfbean;

import static org.junit.Assert.*;

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
    public void testGetPrefix() {
        assertEquals("rdf", Namespaces.getPrefix(RDF.NS));
    }

    @Test
    public void testGetReadableURI() {
        assertEquals("rdf:type", Namespaces.getReadableURI(RDF.NS,"type"));
        assertEquals("<urn:test>", Namespaces.getReadableURI("urn:", "test"));
    }

}
