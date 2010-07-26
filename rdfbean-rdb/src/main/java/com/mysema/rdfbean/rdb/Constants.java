/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * @author tiwe
 *
 */
public final class Constants {
    
    private Constants(){}
    
    private static <T> Set<T> asSet(T... args){
        return new HashSet<T>(Arrays.asList(args));
    }
    
    public static final Set<Class<?>> decimalClasses = Constants.<Class<?>>asSet(Double.class, Float.class, BigDecimal.class);    

    public static final Set<Class<?>> dateClasses = Constants.<Class<?>>asSet(java.sql.Date.class, LocalDate.class);
    
    public static final Set<Class<?>> dateTimeClasses = Constants.<Class<?>>asSet(java.util.Date.class, Timestamp.class, DateTime.class);
    
    public static final Set<Class<?>> timeClasses = Constants.<Class<?>>asSet(java.sql.Time.class, LocalTime.class);
    
    public static final Set<UID> decimalTypes = asSet(XSD.decimalType, XSD.doubleType, XSD.floatType);
    
    public static final Set<UID> integerTypes = asSet(XSD.integerType, XSD.longType, XSD.intType, XSD.shortType, XSD.byteType);
    
    public static final Set<UID> dateTypes = Collections.singleton(XSD.date);
    
    public static final Set<UID> dateTimeTypes = Collections.singleton(XSD.dateTime);
    
    
}
