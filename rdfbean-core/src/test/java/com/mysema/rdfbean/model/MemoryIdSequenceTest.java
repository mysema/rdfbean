package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * MemoryIdSequenceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MemoryIdSequenceTest {

    @Test
    public void testGetNextId() {
        IdSequence idSequence = new MemoryIdSequence();
        assertEquals(1l, idSequence.getNextId());
        assertEquals(2l, idSequence.getNextId());
    }

}
