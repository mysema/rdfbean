/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import static org.junit.Assert.assertEquals;

import java.sql.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * DateTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DateConverterTest {
    
    private Locale defaultLocale;
    
    @Before
    public void setUp(){
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }
    
    @After
    public void tearDown(){
        Locale.setDefault(defaultLocale);
    }
    
    @Test
    public void test(){
        Date date = new Date(0);
        DateConverter converter = new DateConverter();
        String str = converter.toString(date);
        assertEquals(new java.util.Date(date.getTime()), new java.util.Date(converter.fromString(str).getTime()));
        assertEquals(date, converter.fromString(str));
    }

}
