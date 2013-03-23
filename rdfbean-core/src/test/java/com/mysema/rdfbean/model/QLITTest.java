package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QLITTest {

    private static final QLIT lit = new QLIT("lit");

    private static final LIT val = new LIT("X");

    @Test
    public void LtLIT() {
        assertEquals("{lit} < \"X\"", lit.lt(val).toString());
    }

    @Test
    public void GtLIT() {
        assertEquals("{lit} > \"X\"", lit.gt(val).toString());
    }

    @Test
    public void LoeLIT() {
        assertEquals("{lit} <= \"X\"", lit.loe(val).toString());
    }

    @Test
    public void GoeLIT() {
        assertEquals("{lit} >= \"X\"", lit.goe(val).toString());
    }

    @Test
    public void Like() {
        assertEquals("{lit} like \"A%\"", lit.like("A%").toString());
    }

    @Test
    public void Matches() {
        assertEquals("matches({lit},\"A.*\")", lit.matches("A.*").toString());
    }

    @Test
    public void IsEmpty() {
        assertEquals("empty({lit})", lit.isEmpty().toString());
    }

}
