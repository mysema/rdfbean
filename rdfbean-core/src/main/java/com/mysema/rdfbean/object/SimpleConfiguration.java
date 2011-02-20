/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * @author tiwe
 *
 */
class SimpleConfiguration implements Configuration{

    private static final Set<String> buildinNamespaces = new HashSet<String>();

    static {
        buildinNamespaces.add(RDF.NS);
        buildinNamespaces.add(RDFS.NS);
        buildinNamespaces.add(XSD.NS);
        buildinNamespaces.add(OWL.NS);
        buildinNamespaces.add(CORE.NS);
    }

    private final Set<String> restrictedResources = new HashSet<String>(buildinNamespaces);
    
    private final Set<MappedClass> mappedClasses;
    
    private final Set<Class<?>> polymorphicClasses = new HashSet<Class<?>>();
    
    private final Map<UID,MappedClass> uidToMappedClass = new HashMap<UID,MappedClass>();

    private final Map<Class<?>,MappedClass> classToMappedClass = new HashMap<Class<?>,MappedClass>();

    private final ConverterRegistry converterRegistry;
    
    public SimpleConfiguration(
            ConverterRegistry converterRegistry,
            Set<MappedClass> mappedClasses){
        this.converterRegistry = converterRegistry;
        this.mappedClasses = mappedClasses;
        for (MappedClass mappedClass : mappedClasses){
            uidToMappedClass.put(mappedClass.getUID(), mappedClass);
            classToMappedClass.put(mappedClass.getJavaClass(), mappedClass);
            for (MappedClass superClass : mappedClass.getMappedSuperClasses()){
                polymorphicClasses.add(superClass.getJavaClass());
            }
        }
    }
    
    @Override
    public boolean allowCreate(Class<?> clazz) {
        return true;
    }
    
    @Override
    public boolean allowRead(MappedPath path) {
        return true;
    }

    @Override
    public UID createURI(Object instance) {
        Class<?> clazz = instance.getClass();
        UID context = getMappedClass(clazz).getContext();
        if (context != null) {
            return new UID(context.getId() + "#", clazz.getSimpleName() + "-" + UUID.randomUUID().toString());
        }
        return null;
    }

    @Override
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    @Override
    public MappedClass getMappedClass(Class<?> javaClass) {
        return classToMappedClass.get(javaClass);
    }

    @Override
    public List<MappedClass> getMappedClasses(UID uid) {
        MappedClass mappedClass = uidToMappedClass.get(uid);
        return mappedClass != null ? Collections.<MappedClass>singletonList(mappedClass) : Collections.<MappedClass>emptyList();
    }

    @Override
    public Set<MappedClass> getMappedClasses() {
        return mappedClasses;
    }

    public boolean isPolymorphic(Class<?> clazz){
        return polymorphicClasses.contains(clazz);
    }
    
    public boolean isMapped(Class<?> clazz){
        return classToMappedClass.containsKey(clazz);
    }
    
    @Override
    public boolean isRestricted(UID uid) {
        return restrictedResources.contains(uid.getId()) || restrictedResources.contains(uid.ns());
    }

}
