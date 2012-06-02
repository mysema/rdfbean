/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.xsd;

import java.util.HashMap;
import java.util.Map;

import com.google.common.primitives.Primitives;
import com.mysema.converters.BigDecimalConverter;
import com.mysema.converters.BigIntegerConverter;
import com.mysema.converters.BlobConverter;
import com.mysema.converters.BooleanConverter;
import com.mysema.converters.ByteConverter;
import com.mysema.converters.CalendarConverter;
import com.mysema.converters.CharacterConverter;
import com.mysema.converters.Converter;
import com.mysema.converters.DateConverter;
import com.mysema.converters.DateTimeConverter;
import com.mysema.converters.DoubleConverter;
import com.mysema.converters.FloatConverter;
import com.mysema.converters.IntegerConverter;
import com.mysema.converters.LocalDateConverter;
import com.mysema.converters.LocalTimeConverter;
import com.mysema.converters.LongConverter;
import com.mysema.converters.ShortConverter;
import com.mysema.converters.TimeConverter;
import com.mysema.converters.TimestampConverter;
import com.mysema.converters.URIConverter;
import com.mysema.converters.UtilDateConverter;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * @author tiwe
 */
public class ConverterRegistryImpl implements ConverterRegistry{

    public static final ConverterRegistry DEFAULT = new ConverterRegistryImpl();
    
    private final Map<Class<?>, Converter<?>> classToConverter = new HashMap<Class<?>,Converter<?>>();

    private final Map<Class<?>, UID> classToType = new HashMap<Class<?>,UID>();
    
    private final Map<UID, Class<?>> typeToClass = new HashMap<UID, Class<?>>();

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
        
        register(XSD.stringType, new LocaleConverter());
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
        if (!clazz.isPrimitive()){
            typeToClass.put(type, clazz);    
        }        
    }

    private <T> void register(UID type, Converter<T> converter) {
        register(type, converter.getJavaType());
        classToConverter.put(converter.getJavaType(), converter);        
        Class<?> primitiveType = Primitives.unwrap(converter.getJavaType());
        if (!primitiveType.equals(converter.getJavaType())){
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

    @Override
    public Class<?> getClass(UID datatype) {
        return typeToClass.get(datatype);
    }
}
