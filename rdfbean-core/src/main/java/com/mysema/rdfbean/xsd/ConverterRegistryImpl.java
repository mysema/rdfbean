/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * ConverterRegistryImpl provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ConverterRegistryImpl implements ConverterRegistry{

    private final Map<Class<?>,Converter<?>> classToConverter = new HashMap<Class<?>,Converter<?>>();

    private final Map<Class<?>,UID> classToType = new HashMap<Class<?>,UID>();

    public ConverterRegistryImpl(){
        register(XSD.anyURI, new URIConverter());
        register(XSD.booleanType, new BooleanConverter());
        register(XSD.hexBinary, new BlobConverter());
        register(XSD.byteType, new ByteConverter());
        register(XSD.date, new LocalDateConverter());
        register(XSD.date, new DateConverter());
        register(XSD.dateTime, new DateTimeConverter());
        register(XSD.dateTime, new CalendarConverter());
        register(XSD.dateTime, new TimestampConverter());
        register(XSD.dateTime, new UtilDateConverter());
        register(XSD.decimalType, new BigDecimalConverter());
        register(XSD.doubleType, new DoubleConverter());
        // duration
        register(XSD.floatType, new FloatConverter());
        // gDay
        // gMonth
        // gMonthDay
        // gYear
        register(XSD.gYear, new YearConverter());
        // gYearMonth
        register(XSD.integerType, new BigIntegerConverter());
        register(XSD.intType, new IntegerConverter());
        register(XSD.longType, new LongConverter());
        register(XSD.shortType, new ShortConverter());
        register(XSD.stringType, String.class);
        register(XSD.stringType, new CharacterConverter());
        register(XSD.time, new LocalTimeConverter());
        register(XSD.time, new TimeConverter());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromString(String value, Class<T> requiredType){
        if (requiredType.equals(String.class)){
            return (T)value;
        }
        Converter<T> converter = (Converter<T>) classToConverter.get(requiredType);
        if (converter != null){
            return converter.fromString(value);
        }else{
            throw new IllegalArgumentException("No conversion for " + requiredType.getName() + " available");
        }
    }

    @Override
    public UID getDatatype(Class<?> javaClass) {
        return classToType.get(javaClass);
    }

    private <T> void register(UID type, Class<T> clazz) {
        classToType.put(clazz, type);
    }

    private <T> void register(UID type, Converter<T> converter) {
        register(type, converter.getJavaType());
        classToConverter.put(converter.getJavaType(), converter);
        Class<?> primitiveType = ClassUtils.wrapperToPrimitive(converter.getJavaType());
        if (primitiveType != null){
            register(type, primitiveType);
            classToConverter.put(primitiveType, converter);
        }
    }

    @Override
    public boolean supports(Class<?> cl) {
        return classToType.containsKey(cl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> String toString(T javaValue) {
        if (javaValue instanceof String) {
            return (String)javaValue;
        }
        Converter<T> converter = (Converter<T>) classToConverter.get(javaValue.getClass());
        if (converter != null){
            return converter.toString(javaValue);
        }else{
            throw new IllegalArgumentException("No conversion for " + javaValue.getClass().getName() + " available");
        }
    }
}
