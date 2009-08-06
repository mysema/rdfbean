package com.mysema.rdf.demo.foaf.v1;

import java.util.Collection;
import java.util.Locale;

import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.UID;

public interface MProperty {

    void addValue(MValue<?> value);

    void setValue(MValue<?> value);
    
    MValue<Object> getValue();
    
    <T> MValue<T> getValue(Class<T> clazz);
    
    Collection<MValue<Object>> getValues();
    
    <T> Collection<MValue<T>> getValues(Class<T> clazz);
    
    int getValueCount();

    void remove(MValue<?> value);
    
    void removeAll();
    
    String getLabel();
    
    ID getId();
}
