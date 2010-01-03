/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.xsd;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mysema.rdfbean.xsd.Converter;

/**
 * YMDDateTimeConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public enum YMDDateTimeConverter implements Converter<DateTime>{
    
    DAY("yyyyMMdd"),

    HOUR("yyMMddHH"),

    MILLISECOND("yyMMddHHmmssSSS"),

    MINUTE("yyMMddHHmm"),

    MONTH("yyyyMM"),

    SECOND("yyMMddHHmmss"),

    YEAR("yyyy");

    private transient final DateTimeFormatter formatter;
    
    YMDDateTimeConverter(String pattern) {
        this.formatter = DateTimeFormat.forPattern(pattern);
    }
    
    @Override
    public DateTime fromString(String str) {
        return formatter.parseDateTime(str);
    }

    @Override
    public String toString(DateTime object) {
        return formatter.print(object);
    }

}
