/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.xsd;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;

import com.mysema.rdfbean.xsd.Converter;
import com.mysema.rdfbean.xsd.DateConverter;


/**
 * YMDDateConverter provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public enum YMDDateConverter implements Converter<Date>{
    
    YEAR("yyyy"),

    MONTH("yyyyMM"),

    DAY("yyyyMMdd"),

    HOUR("yyMMddHH"),

    MINUTE("yyMMddHHmm"),

    SECOND("yyMMddHHmmss"),

    MILLISECOND("yyMMddHHmmssSSS");
    
    private final DateConverter converter;
    
    YMDDateConverter(String pattern) {
        this.converter = new DateConverter(DateTimeFormat.forPattern(pattern));
    }
    
    @Override
    public Date fromString(String str) {
        return converter.fromString(str);
    }

    @Override
    public String toString(Date date) {
        return converter.toString(date);
    }

}
