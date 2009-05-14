/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.beans.PropertyEditor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.SimpleTypeConverter;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.JodaTimeEditor;
import com.mysema.rdfbean.xsd.Year;
import com.mysema.rdfbean.xsd.YearEditor;

/**
 * Converter provides Literal to/from Object conversion functionality
 *
 * @author tiwe
 * @version $Id$
 */
public class Converter{
    
    private Map<Class<?>,UID> classToType = new HashMap<Class<?>,UID>();
    
    private SimpleTypeConverter converter = new SimpleTypeConverter();
    
    private Map<UID,Class<?>> typeToClass = new HashMap<UID,Class<?>>();
  
    public Converter(){                
        register(XSD.anyURI, URI.class);
        register(XSD.booleanType, Boolean.class);
        register(XSD.byteType, Byte.class);
        register(XSD.date, LocalDate.class);
        register(XSD.dateTime, DateTime.class);
        register(XSD.decimalType, BigDecimal.class);
        register(XSD.doubleType, Double.class);
        // duration
        register(XSD.floatType, Float.class);
        // gDay
        // gMonth
        // gMonthDay
        // gYear
        register(XSD.gYear, Year.class);
        // gYearMonth
        register(XSD.integerType, BigInteger.class);
        register(XSD.intType, Integer.class);
        register(XSD.longType, Long.class);
        register(XSD.shortType, Short.class);
        register(XSD.stringType, String.class);
        register(XSD.time, LocalTime.class);
        
        converter.registerCustomEditor(DateTime.class, new JodaTimeEditor());
        converter.registerCustomEditor(LocalDate.class, JodaTimeEditor.forLocalDate());
        converter.registerCustomEditor(LocalTime.class, JodaTimeEditor.forLocalTime());
        converter.registerCustomEditor(Year.class, new YearEditor());
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(Object value, UID datatype, Class<T> requiredType){
        return (T) converter.convertIfNecessary(value, requiredType);
    }

    public UID getDatatype(Class<?> javaClass) {
        return classToType.get(javaClass);
    }

    public UID getDatatype(Object javaValue) {
        return getDatatype(javaValue.getClass());
    }
    
    private void register(UID type, Class<?> clazz) {
        typeToClass.put(type, clazz);
        classToType.put(clazz, type);
    }
    
    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor){
        converter.registerCustomEditor(requiredType, propertyEditor);
    }

    public boolean supports(Class<?> cl) {
        return typeToClass.containsValue(cl);
    }

    public String toString(Object javaValue) {
        if (javaValue instanceof String) {
            return (String)javaValue;
        } else {
            Class<?> requiredType = javaValue.getClass();
            PropertyEditor pe = converter.findCustomEditor(requiredType, null);
            if (pe == null) {
                pe = converter.getDefaultEditor(requiredType);
            }
            if (pe != null){
                pe.setValue(javaValue);
                return pe.getAsText();    
            }else{
                throw new IllegalArgumentException("No conversion for " + 
                        javaValue.getClass().getName() + 
                        " available");
            }                
        }
    }
    
    
}
