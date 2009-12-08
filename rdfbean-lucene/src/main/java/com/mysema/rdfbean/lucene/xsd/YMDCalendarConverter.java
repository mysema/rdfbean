/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.xsd;

import java.util.Calendar;

import org.joda.time.format.DateTimeFormat;

import com.mysema.rdfbean.xsd.CalendarConverter;
import com.mysema.rdfbean.xsd.Converter;

/**
 * LuceneCalendarConverter provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public enum YMDCalendarConverter implements Converter<Calendar>{
    
    YEAR("yyyy"),

    MONTH("yyyyMM"),

    DAY("yyyyMMdd"),

    HOUR("yyMMddHH"),

    MINUTE("yyMMddHHmm"),

    SECOND("yyMMddHHmmss"),

    MILLISECOND("yyMMddHHmmssSSS");
    
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

}
