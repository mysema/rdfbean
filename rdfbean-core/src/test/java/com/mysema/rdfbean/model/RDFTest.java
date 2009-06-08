package com.mysema.rdfbean.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mysema.rdfbean.TEST;

public class RDFTest {
    
    @Test
    public void testIsContainerMembershipProperty() {
        assertFalse(RDF.isContainerMembershipProperty(new UID(TEST.NS, "foo")));
        assertFalse(RDF.isContainerMembershipProperty(RDF.li));
        assertFalse(RDF.isContainerMembershipProperty(new UID(RDF.NS, "_")));
        assertFalse(RDF.isContainerMembershipProperty(new UID(RDF.NS, "_0")));
        assertTrue(RDF.isContainerMembershipProperty(new UID(RDF.NS, "_1")));
        assertTrue(RDF.isContainerMembershipProperty(new UID(RDF.NS, "_12")));
        assertTrue(RDF.isContainerMembershipProperty(new UID(RDF.NS, "_10120")));
        assertFalse(RDF.isContainerMembershipProperty(new UID(RDF.NS, "_1012a")));
        assertFalse(RDF.isContainerMembershipProperty(new UID(RDF.NS, "_1012a4")));
    }

}
