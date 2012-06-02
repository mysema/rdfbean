package com.mysema.rdfbean.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class CharSetTest {
    
    private static final CharSet WS = CharSet.getInstance(" \t\n\f\r");
    
    private static final CharSet WORD = CharSet.getInstance("a-zA-Z0-9");
    
    @Test
    public void WS() {
        assertTrue(WS.contains(' '));
    }
    
    @Test
    public void WORD() {
        assertTrue(WORD.contains('a'));
        assertTrue(WORD.contains('c'));
        assertTrue(WORD.contains('A'));
        assertTrue(WORD.contains('Z'));
        assertTrue(WORD.contains('2'));
        assertTrue(WORD.contains('9'));
    }

}
