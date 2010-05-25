package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UIDTest {

    @Test
    public void testUIDString() {
	UID uid = new UID(RDF.type.getId());
	assertEquals(RDF.NS, uid.getNamespace());
	assertEquals("type", uid.getLocalName());
    }

}
