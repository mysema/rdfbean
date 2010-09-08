/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

public class LocalDateConverterTest extends AbstractConverterTest<LocalDate>{

    @Override
    Converter<LocalDate> createConverter() {
	return new LocalDateConverter();
    }

    @Override
    LocalDate createValue() {
	return new LocalDate();
    }

    @Test
    public void test(){
        Converter<LocalDate> converter = createConverter();               
        LocalDate dateTime = new LocalDate(2010,1,1);
        assertEquals("2010-01-01", converter.toString(dateTime));        
    }
}
