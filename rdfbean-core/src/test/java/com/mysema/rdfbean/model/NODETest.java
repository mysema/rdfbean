/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NODETest {
    
    @Test
    public void test(){
        NODE uri = RDF.type;
        NODE bnode = new BID();
        NODE lit = new LIT("");
        assertEquals(uri, uri.asURI());
        assertEquals(bnode, bnode.asBNode());
        assertEquals(lit, lit.asLiteral());
    }

}
