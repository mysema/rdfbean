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

/**
 * DateConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CalendarConverter implements Converter<Calendar>{
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = ISODateTimeFormat.dateTime();
    
    private static final DateTimeFormatter FALLBACK = ISODateTimeFormat.dateTimeNoMillis();
    
    private final DateTimeFormatter formatter;
    
    public CalendarConverter(DateTimeFormatter formatter){
        this.formatter = formatter;
    }
    
    public CalendarConverter(){
        this(DEFAULT_FORMATTER);
    }
    
    @Override
    public Calendar fromString(String str) {
        try{
            return formatter.parseDateTime(str).toCalendar(Locale.getDefault());       
        }catch(IllegalArgumentException e){
            return FALLBACK.parseDateTime(str).toCalendar(Locale.getDefault());       
        }        
    }

    @Override
    public String toString(Calendar object) {
        DateTime dateTime = new DateTime(object.getTimeInMillis());
        if (dateTime.getMillisOfSecond() != 0){
            return formatter.print(dateTime);
        }else{
            return FALLBACK.print(dateTime);
        }
    }

    @Override
    public Class<Calendar> getJavaType() {
        return Calendar.class;
    }

//    @Override
//    public UID getType() {
//        return XSD.dateTime;
//    }
}
