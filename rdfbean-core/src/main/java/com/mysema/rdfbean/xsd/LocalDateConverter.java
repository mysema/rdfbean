/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * LocalDateConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LocalDateConverter implements Converter<LocalDate> {

    private final DateTimeFormatter fromStringFmt = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.getDefault());
    
    private final DateTimeFormatter toStringFmt = DateTimeFormat.forPattern("yyyy-MM-ddZ").withZone(DateTimeZone.getDefault());
    
    @Override
    public LocalDate fromString(String str) {
        return fromStringFmt.parseDateTime(str).toLocalDate();
    }

    @Override
    public String toString(LocalDate object) {
        return toStringFmt.print(object);
    }

}
