/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.MappedClasses;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;
import com.mysema.util.ClassPathUtils;

/**
 * Default implementation of the Configuration interface
 *
 * @author sasa
 *
 */
public class DefaultConfiguration implements Configuration {

    private static final Set<String> buildinNamespaces = new HashSet<String>();

    static {
        buildinNamespaces.add(RDF.NS);
        buildinNamespaces.add(RDFS.NS);
        buildinNamespaces.add(XSD.NS);
        buildinNamespaces.add(OWL.NS);
        buildinNamespaces.add(CORE.NS);
    }

    private final Set<MappedClass> mappedClasses = new LinkedHashSet<MappedClass>();

    private final Set<Class<?>> polymorphicClasses = new HashSet<Class<?>>();

    private final ConverterRegistry converterRegistry = new ConverterRegistryImpl();

    private final MappedClassFactory mappedClassFactory;

    private final Set<String> restrictedResources = new HashSet<String>(buildinNamespaces);

    private final Map<UID, List<MappedClass>> type2classes = new HashMap<UID, List<MappedClass>>();

    public DefaultConfiguration(@Nullable String defaultNamespace) {
        this.mappedClassFactory = new MappedClassFactory(defaultNamespace);
    }

    public DefaultConfiguration() {
        this((String)null);
    }

    public DefaultConfiguration(Class<?>... classes) {
        this((String)null);
        addClasses(classes);
    }

    public DefaultConfiguration(Package... packages) {
        this((String)null);
        addPackages(packages);
    }

    public DefaultConfiguration(@Nullable String defaultNamespace, Class<?>... classes) {
        this(defaultNamespace);
        addClasses(classes);
    }

    public DefaultConfiguration(@Nullable String defaultNamespace, Package... packages) {
        this(defaultNamespace);
        addPackages(packages);
    }

    public final void addClasses(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (clazz.getAnnotation(ClassMapping.class) != null){
                MappedClass mappedClass = mappedClassFactory.getMappedClass(clazz);                
                if (mappedClass.getUID() != null) {
                    ClassMapping classMapping = clazz.getAnnotation(ClassMapping.class);
                    if (clazz.isEnum() && !classMapping.parent().equals(Object.class)) {
                        MappedClass parentClass = mappedClassFactory.getMappedClass(classMapping.parent());
                        for (Object constant : clazz.getEnumConstants()) {
                            UID instance = new UID(mappedClass.getClassNs(), ((Enum)constant).name());
                            addClass(instance, parentClass);
                        }
                    }
                    addClass(mappedClass.getUID(), mappedClass);
                }
                for (MappedClass superClass : mappedClass.getMappedSuperClasses()){
                    polymorphicClasses.add(superClass.getJavaClass());
                }
                mappedClasses.add(mappedClass);
            }else{
                throw new IllegalArgumentException("No @ClassMapping annotation for " + clazz.getName());
            }
        }
    }

    private void addClass(UID uid, MappedClass mappedClass) {
        List<MappedClass> classList = type2classes.get(uid);
        if (classList == null) {
            classList = new ArrayList<MappedClass>();
            type2classes.put(uid, classList);
        }
        classList.add(mappedClass);
    }
    
    public final void addPackages(Package... packages) {
        for (Package pack : packages) {
            MappedClasses classes = pack.getAnnotation(MappedClasses.class);
            if (classes != null) {
                addClasses(classes.value());
            }else{
                throw new IllegalArgumentException("No @MappedClasses annotation for " + pack.getName());
            }
        }
    }

    @Override
    public boolean allowCreate(Class<?> clazz) {
        return true;
    }

    public boolean allowRead(MappedPath path) {
        // TODO filter unmapped types?
        return true;
    }

    @Override
    @Nullable
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
        return mappedClassFactory.getMappedClass(javaClass);
    }

    @Override
    public Set<MappedClass> getMappedClasses() {
        return mappedClasses;
    }

    public List<MappedClass> getMappedClasses(UID uid) {
        if (type2classes.containsKey(uid)){
            return type2classes.get(Assert.notNull(uid,"uid"));
        }else{
            return Collections.emptyList();
        }
    }

    public boolean isMapped(Class<?> clazz){
        return clazz.getAnnotation(ClassMapping.class) != null;
    }

    public boolean isPolymorphic(Class<?> clazz){
        return polymorphicClasses.contains(clazz);
    }

    @Override
    public boolean isRestricted(UID uid) {
        return restrictedResources.contains(uid.getId()) || restrictedResources.contains(uid.ns());
    }

    public void scanPackages(Package... packages){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (Package pkg : packages){
            try {
                for (Class<?> cl : ClassPathUtils.scanPackage(classLoader, pkg)){
                    if (cl.getAnnotation(ClassMapping.class) != null){
                        addClasses(cl);
                    }
                }
            } catch (IOException e) {
                throw new ConfigurationException(e);
            }
        }
    }
    
}
