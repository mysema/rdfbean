
/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * DateTimeConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DateTimeConverter implements Converter<DateTime> {

    private final DateTimeFormatter fromStringFmt = ISODateTimeFormat.dateTimeNoMillis();
    private final DateTimeFormatter toStringFmt = ISODateTimeFormat.dateTimeNoMillis();
        
    @Override
    public DateTime fromString(String str) {
        return fromStringFmt.parseDateTime(str);
    }

    @Override
    public String toString(DateTime object) {
        return toStringFmt.print(object);
    }

}
