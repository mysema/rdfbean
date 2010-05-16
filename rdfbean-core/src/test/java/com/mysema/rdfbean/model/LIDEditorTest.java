package com.mysema.rdfbean.model;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * LIDEditorTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LIDEditorTest {

    private LIDEditor editor = new LIDEditor();
    
    @Test
    public void testGetAsText() {
        editor.setValue(new LID("1"));        
        assertEquals("1", editor.getAsText());
    }

    @Test
    public void testSetAsTextString() {
        editor.setAsText("1");
        assertEquals(new LID("1"), editor.getValue());
    }

}
