/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;


/**
 * @author tiwe
 *
 */
public class ConfigurationBuilder {
        
    private final Set<MappedClass> mappedClasses = new HashSet<MappedClass>();
    
    private ConverterRegistry converterRegistry;
    
    private List<FetchStrategy> fetchStrategies = Collections.emptyList();
    
    public Configuration build() {
        if (converterRegistry == null){
            converterRegistry = new ConverterRegistryImpl();
        }
        return new SimpleConfiguration(converterRegistry, fetchStrategies, mappedClasses);
    }

    public MappedClassBuilder addClass(String ns, Class<?> clazz) {
        return addClass(new UID(ns, clazz.getSimpleName()), clazz);
    }
    
    public MappedClassBuilder addClass(UID uid, Class<?> clazz) {
        MappedClass mappedClass = new MappedClass(clazz, uid, Collections.<MappedClass>emptyList());
        mappedClasses.add(mappedClass);
        return new MappedClassBuilder(mappedClass);
    }

    public void setConverterRegistry(ConverterRegistry converterRegistry) {
        this.converterRegistry = converterRegistry;
    }

    public void setFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }
    
    

}
