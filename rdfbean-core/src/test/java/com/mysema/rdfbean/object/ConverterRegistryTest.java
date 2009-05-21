/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.mysema.rdfbean.xsd.Year;


/**
 * ConverterTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ConverterRegistryTest {

    private ConverterRegistry converter = new ConverterRegistry();
    
    @Test
    public void testYear(){
        assertEquals("2000", converter.toString(new Year(2000)));
        assertEquals("-2000", converter.toString(new Year(-2000)));        
    }
    
    @Test
    public void testLocalDate(){
        assertEquals("2000-01-01", converter.toString(new LocalDate(2000,1,1)));    
    }
}
