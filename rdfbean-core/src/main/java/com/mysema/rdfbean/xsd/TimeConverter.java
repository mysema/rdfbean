/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import java.sql.Time;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

public class TimeConverter implements Converter<Time>{

    private final DateTimeFormatter fromStringFmt = ISODateTimeFormat.timeParser().withZone(DateTimeZone.getDefault());
    
    private final DateTimeFormatter toStringFmt = ISODateTimeFormat.time();
        
    @Override
    public Time fromString(String str) {
        return new Time(fromStringFmt.parseDateTime(str).getMillis());
    }

    @Override
    public String toString(Time object) {
        return toStringFmt.print(new LocalTime(object));
    }

    @Override
    public Class<Time> getJavaType() {
        return Time.class;
    }

    @Override
    public UID getType() {
        return XSD.time;
    }

}
