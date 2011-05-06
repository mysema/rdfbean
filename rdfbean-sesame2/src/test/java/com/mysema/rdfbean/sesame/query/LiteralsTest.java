/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.LiteralsDomain;
import com.mysema.rdfbean.domains.LiteralsDomain.Literals;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig(Literals.class)
public class LiteralsTest extends SessionTestBase implements LiteralsDomain{
    
    private final Literals l = Alias.alias(Literals.class);
    
    @Test
    public void test(){
       Literals literals = new Literals();
       literals.booleanValue = true;
       literals.byteValue = (byte)1;
       literals.dateValue = new Date();
       literals.doubleValue = 2.0;
       literals.floatValue = (float)3.0;
       literals.intValue = 4;
       literals.longValue = 5l;
       literals.shortValue = (short)6;
       literals.stringValue = "7";        
       session.save(literals);        
       
       assertEquals(Boolean.valueOf(literals.isBooleanValue()), session.from($(l)).uniqueResult($(l.isBooleanValue())));
       assertEquals(Byte.valueOf(literals.getByteValue()), session.from($(l)).uniqueResult($(l.getByteValue())));
       assertEquals(literals.getDateValue(), session.from($(l)).uniqueResult($(l.getDateValue())));
       assertEquals(Double.valueOf(literals.getDoubleValue()), session.from($(l)).uniqueResult($(l.getDoubleValue())));
       assertEquals(Float.valueOf(literals.getFloatValue()), session.from($(l)).uniqueResult($(l.getFloatValue())));
       assertEquals(Integer.valueOf(literals.getIntValue()), session.from($(l)).uniqueResult($(l.getIntValue())));
       assertEquals(Long.valueOf(literals.getLongValue()), session.from($(l)).uniqueResult($(l.getLongValue())));
       assertEquals(Short.valueOf(literals.getShortValue()), session.from($(l)).uniqueResult($(l.getShortValue())));
       assertEquals(literals.getStringValue(), session.from($(l)).uniqueResult($(l.getStringValue())));
       
    }
    
    @Test
    public void Boolean(){
        Literals literals = new Literals();
        literals.booleanValue = true;
        session.save(literals);
        session.flush();
        session.clear();
        
        // load
        literals = session.get(Literals.class, literals.getId());
        assertTrue(literals.isBooleanValue());
        session.clear();
        
        assertEquals(1l, session.from($(l)).where($(l.isBooleanValue()).eq(true)).count());
        assertEquals(0l, session.from($(l)).where($(l.isBooleanValue()).eq(false)).count());
        
        // change
        literals.booleanValue = false;
        session.save(literals);
        session.flush();
        session.clear();
        
        // reload
        literals = session.get(Literals.class, literals.getId());
        assertFalse(literals.isBooleanValue());
        session.clear();
        
        assertEquals(0l, session.from($(l)).where($(l.isBooleanValue()).eq(true)).count());
        assertEquals(1l, session.from($(l)).where($(l.isBooleanValue()).eq(false)).count());
    }
    
    @Test
    public void LocalDate_2000_01_01(){
        testDatePersistence(new LocalDate(2000, 01, 01));        
    }
    
    @Test
    public void LocalDate_1934_10_10(){
        testDatePersistence(new LocalDate(1934, 10, 10));        
    }
    
    @Test
    public void LocalDate_1900_02_02(){
        testDatePersistence(new LocalDate(1900, 02, 02));        
    }

    @Test
    public void LocalDate_1900_01_03(){
        testDatePersistence(new LocalDate(1900, 01, 03));        
    }
    
    @Test
    public void LocalDate_1900_01_02(){
        testDatePersistence(new LocalDate(1900, 01, 02));        
    }
    
    @Test
    public void LocalDate_1900_01_01(){
        testDatePersistence(new LocalDate(1900, 01, 01));        
    }
        
    @Test
    public void LocalDate_1834_10_10(){
        testDatePersistence(new LocalDate(1834, 10, 10));
    }
    
    @Test
    public void LocalDate_1734_10_10(){
        testDatePersistence(new LocalDate(1734, 10, 10));
    }
    
    @Test
    public void DateTime_2000_01_01(){
        testDateTimePersistence(new DateTime(2000, 1, 1, 0, 0, 0, 0));
    }
    
    @Test
    public void DateTime_2000_01_01_UTC(){
        testDateTimePersistence(new DateTime(2000, 1, 1, 0, 0, 0, 0).withZoneRetainFields(DateTimeZone.UTC));
    }
    
    @Test
    public void DateTime_2000_01_01_Plus_1(){
        testDateTimePersistence(new DateTime(2000, 1, 1, 0, 0, 0, 0).withZoneRetainFields(DateTimeZone.forOffsetHours(1)));
    }
    
    @Test
    public void DateTime_2000_01_01_Plus_2(){
        testDateTimePersistence(new DateTime(2000, 1, 1, 0, 0, 0, 0).withZoneRetainFields(DateTimeZone.forOffsetHours(2)));
    }
    
    @Test
    public void DateTime_1900_01_01(){
        testDateTimePersistence(new DateTime(1900, 1, 1, 0, 0, 0, 0));
    }
    
    @Test
    public void DateTime_1800_01_01(){
        testDateTimePersistence(new DateTime(1800, 1, 1, 0, 0, 0, 0));
    }
    
    @Test
    public void DateTime_1834_10_10(){
        testDateTimePersistence(new LocalDate(1834, 10, 10).toDateTimeAtStartOfDay());
    }
    
    @Test
    public void DateTime_1872_12_31(){
        // 1872-12-31T00:00:00.000+01:39:52
        testDateTimePersistence(new LocalDate(1872, 12, 31).toDateTimeAtStartOfDay());
    }
    
    @Test
    public void DateTime_1872_12_31_UTC(){
        // 1872-12-31T00:00:00.000Z
        testDateTimePersistence(new LocalDate(1872, 12, 31).toDateTimeAtStartOfDay().withZoneRetainFields(DateTimeZone.UTC));
    }
    
    private void testDatePersistence(LocalDate date){
        Literals literals = new Literals();
        literals.localDate = date;
        session.save(literals);
        session.flush();
        session.clear();
        
        // load
        literals = session.get(Literals.class, literals.getId());
        assertEquals(date, literals.localDate);
        session.clear();
        
        // change
        literals.localDate = date.minusDays(1);
        session.save(literals);
        session.flush();
        session.clear();
        
        // reload
        literals = session.get(Literals.class, literals.getId());
        assertEquals(date.minusDays(1), literals.localDate);     
    }
    
    private void testDateTimePersistence(DateTime date){
        Literals literals = new Literals();
        literals.dateTime = date;
        session.save(literals);
        session.flush();
        session.clear();
        System.err.println(date);
        
        // load
        literals = session.get(Literals.class, literals.getId());
        assertEquals(date.toString(),  literals.dateTime.toString());
        assertEquals(date.getMillis(), literals.dateTime.getMillis());
        session.clear();
        
        // change
        literals.dateTime = date.minusDays(1);
        session.save(literals);
        session.flush();
        session.clear();
        
        // reload
        literals = session.get(Literals.class, literals.getId());
        assertEquals(date.minusDays(1).toString(),  literals.dateTime.toString());
        assertEquals(date.minusDays(1).getMillis(), literals.dateTime.getMillis()); 
    }
    
}
