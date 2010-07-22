/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.xsd;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.Converter;
import com.mysema.rdfbean.xsd.UtilDateConverter;


/**
 * YMDDateConverter provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public enum YMDDateConverter implements Converter<Date>{
    
    DAY("yyyyMMdd"),

    HOUR("yyMMddHH"),

    MILLISECOND("yyMMddHHmmssSSS"),

    MINUTE("yyMMddHHmm"),

    MONTH("yyyyMM"),

    SECOND("yyMMddHHmmss"),

    YEAR("yyyy");
    
    private final UtilDateConverter converter;
    
    YMDDateConverter(String pattern) {
        this.converter = new UtilDateConverter(DateTimeFormat.forPattern(pattern));
    }
    
    @Override
    public Date fromString(String str) {
        return converter.fromString(str);
    }

    @Override
    public String toString(Date date) {
        return converter.toString(date);
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
