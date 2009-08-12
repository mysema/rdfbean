package com.mysema.rdfbean.object;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.ClassUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.*;

/**
 * IConverterRegistry provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ConverterRegistryImpl implements ConverterRegistry{

    private Map<Class<?>,Converter<?>> classToConverter = new HashMap<Class<?>,Converter<?>>();
    
    private Map<Class<?>,UID> classToType = new HashMap<Class<?>,UID>();

    public ConverterRegistryImpl(){                
        register(XSD.anyURI, URI.class, new URIConverter());
        register(XSD.booleanType, Boolean.class, new BooleanConverter());
        register(XSD.byteType, Byte.class, new ByteConverter());
        register(XSD.date, LocalDate.class, new LocalDateConverter());
        register(XSD.dateTime, DateTime.class, new DateTimeConverter());        
        register(XSD.dateTime, java.util.Date.class, new DateConverter());
        register(XSD.decimalType, BigDecimal.class, new BigDecimalConverter());
        register(XSD.doubleType, Double.class, new DoubleConverter());
        // duration
        register(XSD.floatType, Float.class, new FloatConverter());
        // gDay
        // gMonth
        // gMonthDay
        // gYear
        register(XSD.gYear, Year.class, new YearConverter());
        // gYearMonth
        register(XSD.integerType, BigInteger.class, new BigIntegerConverter());
        register(XSD.intType, Integer.class, new IntegerConverter());
        register(XSD.longType, Long.class, new LongConverter());
        register(XSD.shortType, Short.class, new ShortConverter());
        register(XSD.stringType, String.class, null);
        register(XSD.stringType, Character.class, new CharacterConverter());
        register(XSD.time, LocalTime.class, new LocalTimeConverter());        
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

    private <T> void register(UID type, Class<T> clazz, @Nullable Converter<T> converter) {        
        classToType.put(clazz, type);
        if (converter != null){
            classToConverter.put(clazz, converter);    
            Class<?> primitiveType = ClassUtils.wrapperToPrimitive(clazz);
            if (primitiveType != null){
                classToConverter.put(primitiveType, converter);
            }
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
