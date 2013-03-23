/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import org.junit.Test;

public class LITTest {

    @Test
    public void ToString() {
        LIT lit1 = new LIT("x");
        LIT lit2 = new LIT("x", Locale.ENGLISH);
        LIT lit3 = new LIT("x", XSD.stringType);
        LIT lit4 = new LIT("x", "en");
        assertNotNull(lit1.toString());
        assertNotNull(lit2.toString());
        assertNotNull(lit3.toString());
        assertNotNull(lit4.toString());
    }

}
