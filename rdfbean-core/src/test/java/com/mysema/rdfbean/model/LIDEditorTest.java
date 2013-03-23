/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LIDEditorTest {

    private LIDEditor editor = new LIDEditor();

    @Test
    public void GetAsText() {
        editor.setValue(new LID("1"));
        assertEquals("1", editor.getAsText());
    }

    @Test
    public void SetAsTextString() {
        editor.setAsText("1");
        assertEquals(new LID("1"), editor.getValue());
    }

}
