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
 * LuceneLocalDateConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public enum YMDDateTimeConverter implements Converter<DateTime>{
    
    YEAR("yyyy"),

    MONTH("yyyyMM"),

    DAY("yyyyMMdd"),

    HOUR("yyMMddHH"),

    MINUTE("yyMMddHHmm"),

    SECOND("yyMMddHHmmss"),

    MILLISECOND("yyMMddHHmmssSSS");

    private final DateTimeFormatter formatter;
    
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
