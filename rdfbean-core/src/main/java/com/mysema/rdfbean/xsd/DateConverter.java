/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * DateConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DateConverter implements Converter<Date>{
    
    private final DateTimeFormatter fromStringFmt = ISODateTimeFormat.dateTimeNoMillis();
    private final DateTimeFormatter toStringFmt = ISODateTimeFormat.dateTimeNoMillis();
        
    @Override
    public Date fromString(String str) {
        return fromStringFmt.parseDateTime(str).toDate();
    }

    @Override
    public String toString(Date object) {
        return toStringFmt.print(new DateTime(object));
    }
}
