/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.mysema.rdfbean.xsd.DateTimeConverter;

/**
 * Functions provides
 *
 * @author tiwe
 * @version $Id$
 */
public final class QueryFunctions {
    
    private static DateTimeConverter dateTime = new DateTimeConverter();
    
    private QueryFunctions(){}

    public static String abs(String str){
        return str.startsWith("-") ? str.substring(1) : str;
    }
    
    public static String ceil(String str){
        return String.valueOf(Math.ceil(Double.valueOf(str)));
    }
    
    public static String charAt(String str, String index){
        return String.valueOf(str.charAt(Integer.parseInt(index)));
    }
    
    public static String concat(String str1, String str2){
        return str1 + str2;
    }
    
    public static String dayOfMonth(String str){
        return String.valueOf(dateTime.fromString(str).getDayOfMonth());
    }
    
    public static String dayOfWeek(String str){
        int dow = dateTime.fromString(str).getDayOfWeek();
        return String.valueOf(dow == 7 ? 1 : dow + 1);
    }
    
    public static String dayOfYear(String str){
        return String.valueOf(dateTime.fromString(str).getDayOfYear());
    }
    
    public static String endsWith(String str1, String str2){
        return Boolean.toString(str1.endsWith(str2));
    }
    
    public static String endsWithIc(String str1, String str2){
        return endsWith(str1.toLowerCase(), str2.toLowerCase());
    }
    
    public static String equalsIgnoreCase(String str1, String str2){
        return Boolean.toString(str1.equalsIgnoreCase(str2));
    }
    
    public static String floor(String str){
        return String.valueOf(Math.floor(Double.valueOf(str)));
    }
    
    public static String hour(String str){
        return String.valueOf(dateTime.fromString(str).getHourOfDay());
    }
    
    public static String indexOf(String str1, String str2){
        return Integer.toString(str1.indexOf(str2), 10);
    }
    
    public static String indexOf(String str1, String str2, String start){
        return Integer.toString(str1.indexOf(str2, Integer.parseInt(start)), 10);
    }
    
    public static String like(String str, String matches){
        matches = matches.replace("%", ".*").replaceAll("_", ".");
        return Boolean.toString(str.matches(matches));
    }
    
    public static String lower(String str){
        return str.toLowerCase(Locale.ENGLISH);
    }
    
    public static String matches(String str, String regex){
        return String.valueOf(str.matches(regex));
    }
    
    public static String millisecond(String str){
        return String.valueOf(dateTime.fromString(str).getMillisOfSecond());
    }
    
    public static String minute(String str){
        return String.valueOf(dateTime.fromString(str).getMinuteOfHour());
    }
    
    public static String month(String str){
        return String.valueOf(dateTime.fromString(str).getMonthOfYear());
    }
    
    public static String yearMonth(String str){
        DateTime date = dateTime.fromString(str);
        return String.valueOf(date.getYear() * 100 + date.getMonthOfYear());
    }
    
    public static String second(String str){
        return String.valueOf(dateTime.fromString(str).getSecondOfMinute());
    }
    
    public static String space(String amount){
        return StringUtils.leftPad("", Integer.parseInt(amount));
    }
    
    public static String sqrt(String str){
        return String.valueOf(Math.sqrt(Double.valueOf(str)));
    }
    
    public static String startsWith(String str1, String str2){
        return Boolean.toString(str1.startsWith(str2));
    }
    
    public static String startsWithIc(String str1, String str2){
        return startsWith(str1.toLowerCase(), str2.toLowerCase());
    }
    
    public static String stringContains(String str, String str2){
        return Boolean.toString(str.contains(str2));
    }
    
    public static String stringContainsIc(String str, String str2){
        return stringContains(str.toLowerCase(), str2.toLowerCase());
    }
    
    public static String stringLength(String str){
        return Integer.toString(str.length(), 10);
    }
    
    public static String substring(String str, String index){
        return str.substring(Integer.parseInt(index));
    }
    
    public static String substring(String str, String index, String index2){
        return str.substring(Integer.parseInt(index), Integer.parseInt(index2));
    }
    
    public static String trim(String str){
        return str.trim();
    }
    
    public static String upper(String str){
        return str.toUpperCase(Locale.ENGLISH);
    }
    
    public static String week(String str){
        return String.valueOf(dateTime.fromString(str).toGregorianCalendar().get(Calendar.WEEK_OF_YEAR));
    }
    
    public static String year(String str){
        return String.valueOf(dateTime.fromString(str).getYear());
    }
    
}
