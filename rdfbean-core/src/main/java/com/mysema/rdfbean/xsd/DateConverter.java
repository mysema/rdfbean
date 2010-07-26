/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import java.sql.Date;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

public class DateConverter implements Converter<Date>{

    private final DateTimeFormatter fromStringFmt = ISODateTimeFormat.dateTimeParser();
    
    private final DateTimeFormatter toStringFmt = DateTimeFormat.forPattern("yyyy-MM-ddZ");
    
    @SuppressWarnings("deprecation")
    @Override
    public Date fromString(String str) {
        LocalDate localDate = fromStringFmt.parseDateTime(str).toLocalDate();
        Date date = new Date(0);
        date.setYear(localDate.getYear()-1900);
        date.setMonth(localDate.getMonthOfYear()-1);
        date.setDate(localDate.getDayOfMonth());
        return date;
    }

    @Override
    public String toString(Date object) {
        return toStringFmt.print(new LocalDate(object));
    }

    @Override
    public Class<Date> getJavaType() {
        return Date.class;
    }

    @Override
    public UID getType() {
        return XSD.date;
    }
    
}
