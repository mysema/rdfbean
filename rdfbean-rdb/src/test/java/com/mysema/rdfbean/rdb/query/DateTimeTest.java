/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import java.sql.Time;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.DateTimeDomain;
import com.mysema.rdfbean.domains.DateTimeDomain.Literals;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig(Literals.class)
public class DateTimeTest extends AbstractRDBTest implements DateTimeDomain{
    
    @Test
    public void test(){
        // FIXME
        Literals literals = new Literals();
        literals.date = new java.util.Date();
        literals.date2 = new java.sql.Date(0);
        literals.dateTime = new DateTime();
        literals.localDate = new LocalDate();
        literals.localTime = new LocalTime();
        literals.time = new Time(0);
        literals.timestamp = new Timestamp(0);
        session.save(literals);
        session.clear();
        
        Literals other = session.get(Literals.class, literals.id);
        assertEquals(literals.date,      other.date);
        assertEquals(literals.date2,     other.date2);
        assertEquals(literals.dateTime.getMillis(),  other.dateTime.getMillis());
        assertEquals(literals.localDate, other.localDate);
        assertEquals(literals.localTime, other.localTime);
        assertEquals(literals.time,      other.time);
        assertEquals(literals.timestamp, other.timestamp);                
        session.clear();

        Literals l = Alias.alias(Literals.class);
        assertEquals(literals.date,      session.from($(l)).where($(l.getDate()).eq(literals.date)).uniqueResult($(l.getDate())));
//        assertEquals(literals.date2,     session.from($(l)).where($(l.getDate2()).eq(literals.date2)).uniqueResult($(l.getDate2())));
        assertEquals(literals.dateTime.getMillis(),  session.from($(l)).where($(l.getDateTime()).eq(literals.dateTime)).uniqueResult($(l.getDateTime())).getMillis());
        assertEquals(literals.localDate, session.from($(l)).where($(l.getLocalDate()).eq(literals.localDate)).uniqueResult($(l.getLocalDate())));
//        assertEquals(literals.localTime, session.from($(l)).where($(l.getLocalTime()).eq(literals.localTime)).uniqueResult($(l.getLocalTime())));
//        assertEquals(literals.time,      session.from($(l)).where($(l.getTime()).eq(literals.time)).uniqueResult($(l.getTime())));
        assertEquals(literals.timestamp, session.from($(l)).where($(l.getTimestamp()).eq(literals.timestamp)).uniqueResult($(l.getTimestamp())));
        
        assertEquals(1, session.from($(l)).where($(l.getDate()).eq(literals.date)).count());
        assertEquals(1, session.from($(l)).where($(l.getDate2()).eq(literals.date2)).count());
        assertEquals(1, session.from($(l)).where($(l.getDateTime()).eq(literals.dateTime)).count());
        assertEquals(1, session.from($(l)).where($(l.getLocalDate()).eq(literals.localDate)).count());
//        assertEquals(1, session.from($(l)).where($(l.getLocalTime()).eq(literals.localTime)).count());
//        assertEquals(1, session.from($(l)).where($(l.getTime()).eq(literals.time)).count());
        assertEquals(1, session.from($(l)).where($(l.getTimestamp()).eq(literals.timestamp)).count());
    }

}