/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;
import com.mysema.rdfbean.xsd.Year;

public class ConverterRegistryTest {

    private ConverterRegistry converter = new ConverterRegistryImpl();
    
    @Test
    public void Year(){
        assertEquals("2000", converter.toString(new Year(2000)));
        assertEquals("-2000", converter.toString(new Year(-2000)));        
    }
    
    @Test
    public void LocalDate(){
        assertEquals("2000-01-01", converter.toString(new LocalDate(2000,1,1)));    
    }
}
