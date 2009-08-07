/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang.StringUtils;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * 
 * JodaTimeEditor provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public class JodaTimeEditor extends PropertyEditorSupport {

    private DateTimeFormatter getAsTextFmt;
    private DateTimeFormatter setAsTextFmt;
     
    public JodaTimeEditor() {
        this(ISODateTimeFormat.dateTimeNoMillis(), ISODateTimeFormat.dateTimeNoMillis() );
    }
    
    public JodaTimeEditor(DateTimeFormatter setAsTextFmt, DateTimeFormatter getAsTextFmt) {
        // TODO get chronology from user/system preferences?
        this.setAsTextFmt = setAsTextFmt;
        this.getAsTextFmt = getAsTextFmt;
//        this.dtf = dtf.withChronology(GJChronology.getInstance(DateTimeZone.getDefault()));
    }
    
    public static JodaTimeEditor forLocalDate(){
        //ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.getDefault()),
        return new JodaTimeEditor(ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.getDefault())
                , DateTimeFormat.forPattern("yyyy-MM-ddZ").withZone(DateTimeZone.getDefault())) {
            @Override
            protected Object convert(DateTime dateTime) {
                return new LocalDate(dateTime);
            }
        };
    }
    
    public static JodaTimeEditor forLocalTime(){
        return new JodaTimeEditor(
                ISODateTimeFormat.timeParser().withZone(DateTimeZone.getDefault()),
                ISODateTimeFormat.timeNoMillis()) {
            @Override
            protected Object convert(DateTime dateTime) {
                return new LocalTime(dateTime);
            }
        };
    }

    protected Object convert(DateTime dateTime) {
        return dateTime;
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        if (value == null) {
            return "";
        } else if (value instanceof ReadableInstant) {
            return getAsTextFmt.print((ReadableInstant) value);
        } else if (value instanceof ReadablePartial) {
            return getAsTextFmt.print((ReadablePartial) value);
        } else {
            throw new IllegalArgumentException("unsupported type: " + value.getClass());
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isEmpty(text)) {
            setValue(null);
        } else {
            setValue(convert(setAsTextFmt.parseDateTime(text)));
        }        
    }
}
