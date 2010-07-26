/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.query.QueryFunctions;

public class CalendarConverterTest {
    
    
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
        Calendar cal = Calendar.getInstance();
        CalendarConverter converter = new CalendarConverter();
        String str = converter.toString(cal);
        
        assertEquals(String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)), QueryFunctions.week(str));
    }

}
