/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.mysema.converters.DateTimeConverter;

/**
 * @author tiwe
 */
public final class QueryFunctions {
    
    private static final DateTimeConverter dateTime = new DateTimeConverter();
        
    public static int dayOfMonth(String str){
        return dateTime.fromString(str).getDayOfMonth();
    }
    
    public static int dayOfWeek(String str){
        int dow = dateTime.fromString(str).getDayOfWeek();
        return dow == 7 ? 1 : dow + 1;
    }
    
    public static int dayOfYear(String str){
        return dateTime.fromString(str).getDayOfYear();
    }
    
    public static double ceil(String str){
        return Math.ceil(Double.valueOf(str));
    }
    
    public static double floor(String str){
        return Math.floor(Double.valueOf(str));
    }
    
    public static int hour(String str){
        return dateTime.fromString(str).getHourOfDay();
    }
    
    public static boolean like(String str, String likeExpression){
        String regex = likeExpression.replace("%", ".*").replaceAll("_", ".");
        return str.matches(regex);
    }
    
    public static int millisecond(String str){
        return dateTime.fromString(str).getMillisOfSecond();
    }
    
    public static int minute(String str){
        return dateTime.fromString(str).getMinuteOfHour();
    }
    
    public static int month(String str){
        return dateTime.fromString(str).getMonthOfYear();
    }
    
    public static int second(String str){
        return dateTime.fromString(str).getSecondOfMinute();
    }
    
    public static String space(int amount){
        return StringUtils.leftPad("", amount);
    }
    
    public static double sqrt(String str){
        return Math.sqrt(Double.valueOf(str));
    }
    
    public static int week(String str){
        return dateTime.fromString(str).toGregorianCalendar().get(Calendar.WEEK_OF_YEAR);
    }
    
    public static int year(String str){
        return dateTime.fromString(str).getYear();
    }
    
    public static int yearMonth(String str){
        DateTime date = dateTime.fromString(str);
        return date.getYear() * 100 + date.getMonthOfYear();
    }
    
    private QueryFunctions(){}
    
}
