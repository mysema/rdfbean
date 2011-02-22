package com.mysema.rdfbean.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.model.QNODE;
import com.mysema.rdfbean.model.RDF;

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
    
}
