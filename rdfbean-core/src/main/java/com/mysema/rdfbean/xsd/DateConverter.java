/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * DateConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DateConverter implements Converter<Date>{
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    
    private final DateTimeFormatter formatter;
    
    public DateConverter(DateTimeFormatter formatter){
        this.formatter = formatter;
    }
    
    public DateConverter(){
        this(DEFAULT_FORMATTER);
    }
    
    @Override
    public Date fromString(String str) {
        return formatter.parseDateTime(str).toDate();
    }

    @Override
    public String toString(Date object) {
        return formatter.print(new DateTime(object.getTime()));
    }
}
