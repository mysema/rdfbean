
/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * DateTimeConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DateTimeConverter implements Converter<DateTime> {

    private static final DateTimeFormatter DEFAULT_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    
    private final DateTimeFormatter formatter;
    
    public DateTimeConverter(DateTimeFormatter formatter){
        this.formatter = formatter;
    }
    
    public DateTimeConverter(){
        this(DEFAULT_FORMATTER);
    }
        
    @Override
    public DateTime fromString(String str) {
        return formatter.parseDateTime(str);
    }

    @Override
    public String toString(DateTime object) {
        return formatter.print(object);
    }

}
