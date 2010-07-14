/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.xsd;

import java.util.Calendar;

import org.joda.time.format.DateTimeFormat;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.CalendarConverter;
import com.mysema.rdfbean.xsd.Converter;

/**
 * YMDCalendarConverter provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public enum YMDCalendarConverter implements Converter<Calendar>{
    
    DAY("yyyyMMdd"),

    HOUR("yyMMddHH"),

    MILLISECOND("yyMMddHHmmssSSS"),

    MINUTE("yyMMddHHmm"),

    MONTH("yyyyMM"),

    SECOND("yyMMddHHmmss"),

    YEAR("yyyy");
    
    private final CalendarConverter converter;
    
    YMDCalendarConverter(String pattern) {
        this.converter = new CalendarConverter(DateTimeFormat.forPattern(pattern));
    }
    
    @Override
    public Calendar fromString(String str) {
        return converter.fromString(str);
    }

    @Override
    public String toString(Calendar date) {
        return converter.toString(date);
    }

    @Override
    public Class<Calendar> getJavaType() {
        return Calendar.class;
    }

    @Override
    public UID getType() {
        return XSD.dateTime;
    }

}
