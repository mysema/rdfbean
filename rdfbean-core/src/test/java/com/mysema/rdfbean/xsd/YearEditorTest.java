package com.mysema.rdfbean.xsd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class YearEditorTest {

    private YearEditor editor = new YearEditor();

    @Test
    public void GetAsText() {
        editor.setValue(new Year(2000));
        assertEquals("2000", editor.getAsText());
    }

    @Test
    public void SetAsTextString() {
        editor.setAsText("2000");
        assertEquals(new Year(2000), editor.getValue());
    }

}
