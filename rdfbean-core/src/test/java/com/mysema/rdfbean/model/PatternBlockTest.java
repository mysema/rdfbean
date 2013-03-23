package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class PatternBlockTest {

    @Test
    public void Exists() {
        assertEquals("exists { {s} {p} {o} .  }", Blocks.SPO.exists().toString());
    }

    @Test
    public void Equals() {
        assertFalse(Blocks.SPOC.equals(Blocks.SPO));
    }

}
