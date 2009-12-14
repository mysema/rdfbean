/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.xsd.DateTimeConverter;

/**
 * Functions provides
 *
 * @author tiwe
 * @version $Id$
 */
public final class QueryFunctions {
    
    private static final DateTimeConverter dateTime = new DateTimeConverter();
    
    private QueryFunctions(){}

    public static final String abs(String str){
        return str.startsWith("-") ? str.substring(1) : str;
    }
    
    public static final String ceil(String str){
        return String.valueOf(Math.ceil(Double.valueOf(str)));
    }
    
    public static final String charAt(String str, String index){
        return String.valueOf(str.charAt(Integer.parseInt(index)));
    }
    
    public static final String concat(String str1, String str2){
        return str1 + str2;
    }
    
    public static final String dayOfMonth(String str){
        return String.valueOf(dateTime.fromString(str).getDayOfMonth());
    }
    
    public static final String dayOfWeek(String str){
        int dow = dateTime.fromString(str).getDayOfWeek();
        return String.valueOf(dow == 7 ? 1 : dow + 1);
    }
    
    public static final String dayOfYear(String str){
        return String.valueOf(dateTime.fromString(str).getDayOfYear());
    }
    
    public static final String endsWith(String str1, String str2){
        return Boolean.toString(str1.endsWith(str2));
    }
    
    public static final String endsWithIc(String str1, String str2){
        return endsWith(str1.toLowerCase(), str2.toLowerCase());
    }
    
    public static final String equalsIgnoreCase(String str1, String str2){
        return Boolean.toString(str1.equalsIgnoreCase(str2));
    }
    
    public static final String floor(String str){
        return String.valueOf(Math.floor(Double.valueOf(str)));
    }
    
    public static final String hour(String str){
        return String.valueOf(dateTime.fromString(str).getHourOfDay());
    }
    
    public static final String indexOf(String str1, String str2){
        return Integer.toString(str1.indexOf(str2), 10);
    }
    
    public static final String indexOf(String str1, String str2, String start){
        return Integer.toString(str1.indexOf(str2, Integer.parseInt(start)), 10);
    }
    
    public static final String like(String str, String matches){
        matches = matches.replace("%", ".*").replaceAll("_", ".");
        return Boolean.toString(str.matches(matches));
    }
    
    public static final String lower(String str){
        return str.toLowerCase();
    }
    
    public static final String matches(String str, String regex){
        return String.valueOf(str.matches(regex));
    }
    
    public static final String millisecond(String str){
        return String.valueOf(dateTime.fromString(str).getMillisOfSecond());
    }
    
    public static final String minute(String str){
        return String.valueOf(dateTime.fromString(str).getMinuteOfHour());
    }
    
    public static final String month(String str){
        return String.valueOf(dateTime.fromString(str).getMonthOfYear());
    }
    
    public static final String second(String str){
        return String.valueOf(dateTime.fromString(str).getSecondOfMinute());
    }
    
    public static final String space(String amount){
        return StringUtils.leftPad("", Integer.parseInt(amount));
    }
    
    public static final String sqrt(String str){
        return String.valueOf(Math.sqrt(Double.valueOf(str)));
    }
    
    public static final String startsWith(String str1, String str2){
        return Boolean.toString(str1.startsWith(str2));
    }
    
    public static final String startsWithIc(String str1, String str2){
        return startsWith(str1.toLowerCase(), str2.toLowerCase());
    }
    
    public static final String stringContains(String str, String str2){
        return Boolean.toString(str.contains(str2));
    }
    
    public static final String stringLength(String str){
        return Integer.toString(str.length(), 10);
    }
    
    public static final String substring(String str, String index){
        return str.substring(Integer.parseInt(index));
    }
    
    public static final String substring(String str, String index, String index2){
        return str.substring(Integer.parseInt(index), Integer.parseInt(index2));
    }
    
    public static final String trim(String str){
        return str.trim();
    }
    
    public static final String upper(String str){
        return str.toUpperCase();
    }
    
    public static final String week(String str){
        return String.valueOf(dateTime.fromString(str).getWeekOfWeekyear());
    }
    
    public static final String year(String str){
        return String.valueOf(dateTime.fromString(str).getYear());
    }
    
}
