/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MemoryIdSequenceTest {

    @Test
    public void GetNextId() {
        IdSequence idSequence = new MemoryIdSequence();
        assertEquals(1l, idSequence.getNextId());
        assertEquals(2l, idSequence.getNextId());
    }

}
