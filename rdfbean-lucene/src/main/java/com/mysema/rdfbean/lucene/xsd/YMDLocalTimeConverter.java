/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.xsd;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mysema.rdfbean.xsd.Converter;

/**
 * YMDLocalTimeConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public enum YMDLocalTimeConverter implements Converter<LocalTime>{
    
    HOUR("HH"),

    MILLISECOND("HHmmssSSS"),

    MINUTE("HHmm"),

    SECOND("HHmmss");

    private final DateTimeFormatter formatter;
    
    YMDLocalTimeConverter(String pattern) {
        this.formatter = DateTimeFormat.forPattern(pattern);
    }
    
    @Override
    public LocalTime fromString(String str) {
        return formatter.parseDateTime(str).toLocalTime();
    }

    @Override
    public String toString(LocalTime object) {
        return formatter.print(object);
    }

    @Override
    public Class<LocalTime> getJavaType() {
        return LocalTime.class;
    }

//    @Override
//    public UID getType() {
//        return XSD.time;
//    }

}
