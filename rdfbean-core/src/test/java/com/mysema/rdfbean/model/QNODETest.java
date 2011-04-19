package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.query.types.Predicate;

public class QNODETest {
    
    @Test
    public void In_Objects(){
        Predicate p = QNODE.s.in(RDF.first, RDF.rest);
        assertEquals(QNODE.s.eq(RDF.first).or(QNODE.s.eq(RDF.rest)), p);
    }

    @Test(expected=IllegalArgumentException.class)
    public void In_Empty_Is_Not_Allowed(){
        QNODE.s.in();
    }
    
    @Test
    public void Asc_Order(){
        assertEquals("{o} ASC", QNODE.o.asc().toString());
    }
    
    @Test
    public void Desc_Order(){
        assertEquals("{o} DESC", QNODE.o.desc().toString());
    }
 
    @Test
    public void NotIn(){
        assertEquals(
            "{s} != http://www.w3.org/1999/02/22-rdf-syntax-ns#type && {s} != http://www.w3.org/1999/02/22-rdf-syntax-ns#text", 
            QNODE.s.notIn(RDF.type, RDF.text).toString());
    }
    
}
