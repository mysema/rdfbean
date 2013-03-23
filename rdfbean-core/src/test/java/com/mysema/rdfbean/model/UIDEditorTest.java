package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UIDEditorTest {

    @Test
    public void Set_As_Text_And_Get_Value() {
        UIDEditor editor = new UIDEditor();
        editor.setAsText(RDF.type.getId());
        assertEquals(RDF.type, editor.getValue());
    }

    @Test
    public void Set_Value_And_Get_As_Text() {
        UIDEditor editor = new UIDEditor();
        editor.setValue(RDF.type);
        assertEquals(RDF.type.getId(), editor.getAsText());
    }

}
