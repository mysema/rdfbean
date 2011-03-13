/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import com.mysema.converters.DateTimeConverter;
import com.mysema.query.types.Ops;

public class QueryFunctionsTest {
    
    private static final DateTime dateTime = new DateTime();
    
    private static final String dateTimeString = new DateTimeConverter().toString(dateTime);
    
    @Test
    public void  test(){
        Set<String> functions = new HashSet<String>();
        for (Method m : QueryFunctions.class.getMethods()){
            functions.add(m.getName().toUpperCase());
        }
        
        List<Field> fields = new ArrayList<Field>();
        fields.addAll(Arrays.asList(Ops.class.getFields()));
        fields.addAll(Arrays.asList(Ops.AggOps.class.getFields()));
        fields.addAll(Arrays.asList(Ops.DateTimeOps.class.getFields()));
        fields.addAll(Arrays.asList(Ops.MathOps.class.getFields()));
        fields.addAll(Arrays.asList(Ops.StringOps.class.getFields()));
        
        int available = 0;
        int missing = 0;        
        for (Field field : fields){
            String name = field.getName().replace("_", "");
            if (!functions.contains(name)){
                missing++;
            }else{
                available++;
            }
        }
        System.err.println(missing + " functions missing, " + available + " available");
    }
    
    @Test
    public void DayOfMonth() {
        assertEquals(dateTime.getDayOfMonth(), QueryFunctions.dayOfMonth(dateTimeString));
    }

    @Test
    public void DayOfWeek() {
        int dow = dateTime.getDayOfWeek();
        assertEquals(dow == 7 ? 1 : dow + 1, QueryFunctions.dayOfWeek(dateTimeString));
    }

    @Test
    public void DayOfYear() {
        assertEquals(dateTime.getDayOfYear(), QueryFunctions.dayOfYear(dateTimeString));
    }

    @Test
    public void Ceil() {
        assertEquals(1.0, QueryFunctions.ceil("0.5"), 0.01);
    }

    @Test
    public void Floor() {
        assertEquals(0.0, QueryFunctions.floor("0.5"), 0.01);
    }

    @Test
    public void Hour() {
        assertEquals(dateTime.getHourOfDay(), QueryFunctions.hour(dateTimeString));
    }

    @Test
    public void Like() {
        assertTrue(QueryFunctions.like("XXX", "X%"));
    }

    @Test
    public void Millisecond() {
        assertEquals(dateTime.getMillisOfSecond(), QueryFunctions.millisecond(dateTimeString));
    }

    @Test
    public void Minute() {
        assertEquals(dateTime.getMinuteOfHour(), QueryFunctions.minute(dateTimeString));
    }

    @Test
    public void Month() {
        assertEquals(dateTime.getMonthOfYear(), QueryFunctions.month(dateTimeString));
    }

    @Test
    public void Second() {
        assertEquals(dateTime.getSecondOfMinute(), QueryFunctions.second(dateTimeString));
    }

    @Test
    public void Space() {
        assertEquals("   ", QueryFunctions.space(3));
    }

    @Test
    public void Sqrt() {
        assertEquals(2.0, QueryFunctions.sqrt("4"), 0.01);
    }

    @Test
    public void Week() {
        assertEquals(dateTime.toGregorianCalendar().get(Calendar.WEEK_OF_YEAR), QueryFunctions.week(dateTimeString));
    }

    @Test
    public void Year() {
        assertEquals(dateTime.getYear(), QueryFunctions.year(dateTimeString));
    }

    @Test
    public void YearMonth() {
        assertEquals(dateTime.getYear() * 100 + dateTime.getMonthOfYear(), QueryFunctions.yearMonth(dateTimeString));
    }

}
