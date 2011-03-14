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

    @Test(expected=UnsupportedOperationException.class)
    public void LIT_As_Resource(){
        new LIT("X").asResource();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void LIT_As_URI(){
        new LIT("X").asURI();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void LIT_As_Blank(){
        new LIT("X").asBNode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void BID_As_LIT(){
        new BID().asLiteral();
    }

}
