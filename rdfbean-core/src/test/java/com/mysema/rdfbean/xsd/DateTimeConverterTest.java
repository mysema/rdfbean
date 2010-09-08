/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class DateTimeConverterTest extends AbstractConverterTest<DateTime>{

    @Override
    Converter<DateTime> createConverter() {
	return new DateTimeConverter();
    }

    @Override
    DateTime createValue() {
	DateTime val = new DateTime();
	return val.minusMillis(val.getMillisOfSecond());
    }

    @Test
    public void test(){
        Converter<DateTime> converter = createConverter();        
        
        DateTime dateTime = new DateTime(2010,1,1,0,0,0,0, DateTimeZone.UTC);
        assertEquals("2010-01-01T00:00:00Z", converter.toString(dateTime));
        
        dateTime = new DateTime(2010,1,1,0,0,0,0, DateTimeZone.forOffsetHours(2));
        assertEquals("2010-01-01T00:00:00+02:00", converter.toString(dateTime));
        //1851-01-01T00:00:00+01:39:52
        //2010-09-08T19:20:40.500+03:00
    }
    
}
