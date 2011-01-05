
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

    private static final DateTimeFormatter DEFAULT_FORMATTER = ISODateTimeFormat.dateTime();
    
    private static final DateTimeFormatter FALLBACK = ISODateTimeFormat.dateTimeNoMillis();
    
    private final DateTimeFormatter formatter;
    
    public DateTimeConverter(DateTimeFormatter formatter){
        this.formatter = formatter;
    }
    
    public DateTimeConverter(){
        this(DEFAULT_FORMATTER);
    }
        
    @Override
    public DateTime fromString(String str) {
        try{
            return formatter.parseDateTime(str);    
        }catch(IllegalArgumentException e){
            return FALLBACK.parseDateTime(str);
        }    
    }

    @Override
    public String toString(DateTime dateTime) {
        if (dateTime.getMillisOfSecond() != 0){
            return formatter.print(dateTime);
        }else{
            return FALLBACK.print(dateTime);
        }
    }

    @Override
    public Class<DateTime> getJavaType() {
        return DateTime.class;
    }

//    @Override
//    public UID getType() {
//        return XSD.dateTime;
//    }

}
