package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FormatTest {

    @Test
    public void Get_Format() {
        for (Format format : Format.values()) {
            assertEquals(format, Format.getFormat(format.getMimetype(), Format.RDFXML));
        }
    }

}
