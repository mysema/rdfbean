/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;


/**
 * @author tiwe
 *
 */
public class ConfigurationBuilder {
        
    private final Map<Class<?>,MappedClass> mappedClasses = new HashMap<Class<?>,MappedClass>();
    
    @Nullable
    private ConverterRegistry converterRegistry;
    
    private List<FetchStrategy> fetchStrategies = new ArrayList<FetchStrategy>();
    
    public Configuration build() {
        if (converterRegistry == null){
            converterRegistry = new ConverterRegistryImpl();
        }
        
        // populate mappedSuperClasses
        for (MappedClass mappedClass : mappedClasses.values()){
            if (!mappedClass.getJavaClass().getSuperclass().equals(Object.class)){
                MappedClass mappedSuperClass = mappedClasses.get(mappedClass.getJavaClass().getSuperclass());
                if (mappedSuperClass != null){
                    mappedClass.getMappedSuperClasses().add(mappedSuperClass);
                }
            }
        }
        
        // merge data
        for (MappedClass mappedClass : mappedClasses.values()){
            Deque<MappedClass> supers = new ArrayDeque<MappedClass>(mappedClass.getMappedSuperClasses());
            while (!supers.isEmpty()){
                MappedClass mappedSuperClass = supers.pop();
                supers.addAll(mappedSuperClass.getMappedSuperClasses());
                for (MappedPath path : mappedSuperClass.getProperties()) {
                    MappedProperty<?> property = (MappedProperty<?>) path.getMappedProperty().clone();
                    property.resolve(mappedClass);
                    mappedClass.addMappedPath(new MappedPath(property,
                            path.getPredicatePath(), !mappedClass.equals(property.getDeclaringClass())));
                }
            }
        }
        
        return new SimpleConfiguration(converterRegistry, fetchStrategies, new HashSet<MappedClass>(mappedClasses.values()));
    }

    public MappedClassBuilder addClass(Class<?> clazz) {
        return addClass("java:"+clazz.getName().replace('$', '.')+"#", clazz);
    }
        
    public MappedClassBuilder addClass(String ns, Class<?> clazz) {
        return addClass(new UID(ns, clazz.getSimpleName()), clazz);
    }
    
    public MappedClassBuilder addClass(UID uid, Class<?> clazz) {
        MappedClass mappedClass = new MappedClass(clazz, uid, new ArrayList<MappedClass>());
        mappedClasses.put(clazz,mappedClass);
        return new MappedClassBuilder(mappedClass);
    }
    
    public ConfigurationBuilder addFetchStrategy(FetchStrategy fetchStrategy){
        fetchStrategies.add(fetchStrategy);
        return this;
    }
    
    public void setConverterRegistry(ConverterRegistry converterRegistry) {
        this.converterRegistry = converterRegistry;
    }

    public void setFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }
   

}
