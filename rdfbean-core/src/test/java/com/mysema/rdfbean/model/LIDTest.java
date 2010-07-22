package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * LIDTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LIDTest {
    
    private LID lid = new LID("1");

    @Test
    public void testHashCode() {
        LID lid2 = new LID("1");
        assertEquals(lid, lid2);
    }

    @Test
    public void testEqualsObject() {
        LID lid2 = new LID("1");
        assertEquals(lid.hashCode(), lid2.hashCode());
    }

}
