/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * LocalTimeConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LocalTimeConverter implements Converter<LocalTime> {

    private final DateTimeFormatter fromStringFmt = ISODateTimeFormat.timeParser().withZone(DateTimeZone.getDefault());
    
    private final DateTimeFormatter toStringFmt = ISODateTimeFormat.timeNoMillis();
        
    @Override
    public LocalTime fromString(String str) {
        return fromStringFmt.parseDateTime(str).toLocalTime();
    }

    @Override
    public String toString(LocalTime object) {
        return toStringFmt.print(object);
    }

}
