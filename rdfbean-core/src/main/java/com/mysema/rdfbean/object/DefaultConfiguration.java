/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.Context;
import com.mysema.rdfbean.annotations.MappedClasses;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;

/**
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

    private Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

    private Map<String, List<Class<?>>> type2classes = new HashMap<String, List<Class<?>>>();
    
    private UID defaultContext;
    
    private Set<String> restrictedResources = new HashSet<String>(buildinNamespaces);
    
    public DefaultConfiguration() {}
    
    public DefaultConfiguration(Class<?>... classes) {
        addClasses(classes);
    }
    
    public DefaultConfiguration(boolean asPackages, Class<?>... classes) {
        if (asPackages) {
            for (Class<?> clazz : classes) {
                addPackages(clazz.getPackage());
            }
        } else {
            addClasses(classes);
        }
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

    /* (non-Javadoc)
     * @see com.mysema.rdfbean.object.ExecutionContext#allowRead(com.mysema.rdfbean.object.MappedPath)
     */
    public boolean allowRead(MappedPath path) {
        // TODO filter unmapped types?
        return true;
    }
	
	@Override
	public boolean allowCreate(Class<?> clazz) {
	    return true;
	}
	
	@Override
    public Set<Class<?>> getMappedClasses() {
        return classes;
    }

    public List<Class<?>> getMappedClasses(UID uid) {
	    return type2classes.get(Assert.notNull(uid).getId());
	}

    @Override
    public UID getContext(Class<?> javaClass) {
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

    public void setDefaultContext(String ctx) {
        this.defaultContext = new UID(ctx);
    }

    @Override
    public boolean isRestricted(UID uid) {
        return restrictedResources.contains(uid.getId()) || restrictedResources.contains(uid.ns());
    }

    @Override
    public UID createURI(Object instance) {
        Class<?> clazz = instance.getClass();
        UID context = getContext(clazz);
        if (context != null) {
            return new UID(context.getId() + "#", clazz.getSimpleName() + "-" + UUID.randomUUID().toString());
        }
        return null;
    }
    
}
