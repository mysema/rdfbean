/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.util.Calendar;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.mysema.rdfbean.model.UID;

/**
 * DateConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CalendarConverter implements Converter<Calendar>{
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    
    private final DateTimeFormatter formatter;
    
    public CalendarConverter(DateTimeFormatter formatter){
        this.formatter = formatter;
    }
    
    public CalendarConverter(){
        this(DEFAULT_FORMATTER);
    }
    
    @Override
    public Calendar fromString(String str) {
        return formatter.parseDateTime(str).toCalendar(Locale.getDefault());        
    }

    @Override
    public String toString(Calendar object) {
        return formatter.print(new DateTime(object.getTimeInMillis()));
    }

    @Override
    public Class<Calendar> getJavaType() {
        return Calendar.class;
    }

    @Override
    public UID getType() {
        // TODO Auto-generated method stub
        return null;
    }
}
