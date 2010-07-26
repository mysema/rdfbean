/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PredicateCacheTest {

    @Test
    public void testToString() {
	PredicateCache cache = new PredicateCache();
	assertNotNull(cache.toString());
	cache.add(new STMT(RDF.type,RDF.type,RDF.Property));
	assertNotNull(cache.toString());
	cache.add(new STMT(RDF.type,RDF.first,RDF.Property));
	assertNotNull(cache.toString());
    }

}
