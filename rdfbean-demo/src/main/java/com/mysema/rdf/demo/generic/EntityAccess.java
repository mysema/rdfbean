package com.mysema.rdf.demo.generic;

import java.util.Collection;
import java.util.Locale;

import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public interface EntityAccess<T> {
    
    void addValue(UID property, Locale locale, String value);

    void addValue(UID property, Object value);
    
    <R> R getValue(Class<R> class1, Locale locale, UID property);

    <R> R getValue(Class<R> clazz, UID property);

    <R> Collection<R> getValues(Class<R> clazz, UID... properties);

    void removeValue(UID property, Object value);

    Iterable<PropertyAccess> getProperties();
    
    PropertyAccess getProperty(UID uid);

    Iterable<STMT> getStatements();
    
}