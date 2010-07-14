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

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * DateConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class UtilDateConverter implements Converter<Date>{
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = ISODateTimeFormat.dateTime();
    
    private static final DateTimeFormatter FALLBACK = ISODateTimeFormat.dateTimeNoMillis();
    
    private final DateTimeFormatter formatter;
    
    public UtilDateConverter(DateTimeFormatter formatter){
        this.formatter = formatter;
    }
    
    public UtilDateConverter(){
        this(DEFAULT_FORMATTER);
    }
    
    @Override
    public Date fromString(String str) {
        try{
            return formatter.parseDateTime(str).toDate();    
        }catch(IllegalArgumentException e){
            return FALLBACK.parseDateTime(str).toDate();
        }        
    }

    @Override
    public String toString(Date object) {
        DateTime dateTime = new DateTime(object);
        if (dateTime.getMillisOfSecond() != 0){
            return formatter.print(dateTime);
        }else{
            return FALLBACK.print(dateTime);
        }
    }

    @Override
    public Class<Date> getJavaType() {
        return Date.class;
    }

    @Override
    public UID getType() {
        return XSD.dateTime;
    }
}
