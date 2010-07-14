/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.sql.Timestamp;

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
public class TimestampConverter implements Converter<Timestamp>{
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = ISODateTimeFormat.dateTime();
    
    private static final DateTimeFormatter FALLBACK = ISODateTimeFormat.dateTimeNoMillis();
    
    private final DateTimeFormatter formatter;
    
    public TimestampConverter(DateTimeFormatter formatter){
        this.formatter = formatter;
    }
    
    public TimestampConverter(){
        this(DEFAULT_FORMATTER);
    }
    
    @Override
    public Timestamp fromString(String str) {
        try{
            return new Timestamp(formatter.parseDateTime(str).getMillis());    
        }catch(IllegalArgumentException e){
            return new Timestamp(FALLBACK.parseDateTime(str).getMillis());
        }
    }

    @Override
    public String toString(Timestamp object) {
        DateTime dateTime = new DateTime(object);
        if (dateTime.getMillisOfSecond() != 0){
            return formatter.print(dateTime);
        }else{
            return FALLBACK.print(dateTime);
        }
    }

    @Override
    public Class<Timestamp> getJavaType() {
        return Timestamp.class;
    }

    @Override
    public UID getType() {
        return XSD.dateTime;
    }
}
