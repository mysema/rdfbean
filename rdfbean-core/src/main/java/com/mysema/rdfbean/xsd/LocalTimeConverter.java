/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


/**
 * LocalTimeConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LocalTimeConverter implements Converter<LocalTime> {

    private final DateTimeFormatter fromStringFmt = ISODateTimeFormat.timeParser().withZone(DateTimeZone.getDefault());
    
    private final DateTimeFormatter toStringFmt = ISODateTimeFormat.time();
        
    @Override
    public LocalTime fromString(String str) {
        return fromStringFmt.parseDateTime(str).toLocalTime();
    }

    @Override
    public String toString(LocalTime object) {
        return toStringFmt.print(object);
    }

    @Override
    public Class<LocalTime> getJavaType() {
        return LocalTime.class;
    }

    @Override
    public UID getType() {
        return XSD.time;
    }

}
