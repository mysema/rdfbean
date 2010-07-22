package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * NODETest provides
 *
 * @author tiwe
 * @version $Id$
 */
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
