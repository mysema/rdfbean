/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.*;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.Context;
import com.mysema.rdfbean.annotations.MappedClasses;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.object.identity.IdentityService;
import com.mysema.rdfbean.object.identity.MemoryIdentityService;
import com.mysema.rdfbean.owl.OWL;

/**
 * Default implementation of the Configuration interface
 * 
 * @author sasa
 * 
 */
public class DefaultConfiguration implements Configuration {
    
    private final static Set<String> buildinNamespaces = new HashSet<String>();

    static {
        buildinNamespaces.add(RDF.NS);
        buildinNamespaces.add(RDFS.NS);
        buildinNamespaces.add(XSD.NS);
        buildinNamespaces.add(OWL.NS);
        buildinNamespaces.add(CORE.NS);
    }

    private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

    private final ConverterRegistry converterRegistry = new ConverterRegistryImpl();
    
    private IdentityService identityService = MemoryIdentityService.instance();
    
    @Nullable
    private List<FetchStrategy> fetchStrategies;
    
    @Nullable
    private UID defaultContext;
    
    private final Set<String> restrictedResources = new HashSet<String>(buildinNamespaces);
    
    private final Map<String, List<Class<?>>> type2classes = new HashMap<String, List<Class<?>>>();
    
    public DefaultConfiguration() {}
    
    public DefaultConfiguration(boolean asPackages, Class<?>... classes) {
        if (asPackages) {
            for (Class<?> clazz : classes) {
                addPackages(clazz.getPackage());
            }
        } else {
            addClasses(classes);
        }
    }
    
    public DefaultConfiguration(Class<?>... classes) {
        addClasses(classes);
    }
    
    public DefaultConfiguration(Package... packages) {
        addPackages(packages);
    }

    public void addClasses(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            UID uid = MappedClass.getUID(clazz);
            if (uid != null) {
                String uri = uid.getId();
                List<Class<?>> classList = type2classes.get(uri);
                if (classList == null) {
                    classList = new ArrayList<Class<?>>();
                    type2classes.put(uri, classList);
                }
                classList.add(clazz);
                this.classes.add(clazz);
            }
        }
    }

    public void addPackages(Package... packages) {
        for (Package pack : packages) {
            MappedClasses mappedClasses = pack.getAnnotation(MappedClasses.class);
            if (mappedClasses != null) {
                addClasses(mappedClasses.value());
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
    public UID createURI(Object instance) {
        Class<?> clazz = instance.getClass();
        UID context = getContext(clazz, null);
        if (context != null) {
            return new UID(context.getId() + "#", clazz.getSimpleName() + "-" + UUID.randomUUID().toString());
        }
        return null;
    }

    @Override
    public UID getContext(Class<?> javaClass, @Nullable ID subject) {
        Context ctxAnno = javaClass.getAnnotation(Context.class);
        if (ctxAnno == null) {
            Package pack = javaClass.getPackage();
            ctxAnno = pack.getAnnotation(Context.class);
        }
        if (ctxAnno != null) {
            return new UID(ctxAnno.value());
        } else {
            return defaultContext;
        }
    }

    @Override
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    @Override
    public Set<Class<?>> getMappedClasses() {
        return classes;
    }

    public List<Class<?>> getMappedClasses(UID uid) {
        return type2classes.get(Assert.notNull(uid).getId());
    }

    @Override
    public boolean isRestricted(UID uid) {
        return restrictedResources.contains(uid.getId()) || restrictedResources.contains(uid.ns());
    }

    public void setDefaultContext(String ctx) {
        this.defaultContext = new UID(ctx);
    }

    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public void setFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }

    public List<FetchStrategy> getFetchStrategies() {
        return fetchStrategies;
    }
    
}
