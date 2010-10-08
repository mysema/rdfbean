/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UIDTest {

    @Test
    public void UIDString() {
	UID uid = new UID(RDF.type.getId());
	assertEquals(RDF.NS, uid.getNamespace());
	assertEquals("type", uid.getLocalName());
    }

}
