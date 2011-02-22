package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PatternBlockTest {
    
    @Test
    public void Exists(){
        assertEquals("exists { {s} {p} {o} .  }", Blocks.SPO.exists().toString());
    }

}
