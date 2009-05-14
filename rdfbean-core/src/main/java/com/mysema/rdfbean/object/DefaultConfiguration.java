/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.Context;
import com.mysema.rdfbean.annotations.MappedClasses;
import com.mysema.rdfbean.model.*;
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
    
    private String basePath;
    
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
    
    /* (non-Javadoc)
     * @see com.mysema.rdfbean.object.ExecutionContext#createInstance(org.openrdfbean.model.Resource, java.util.Collection, java.lang.Class)
     */
	@Override
    public <T, N, R extends N, U extends R> T createInstance(R subject, 
            Collection<R> types, 
            Class<T> requiredType,
            RDFBinder<R> binder, 
            Dialect<N, R, ?, U, ?, ?> dialect) {
        T instance;
        Class<? extends T> actualType = matchType(types, requiredType, dialect);
        if (actualType != null) {
            if (!allowCreate(actualType)) {
                instance = null;
            } else {
                try {
                    MappedClass mappedClass = MappedClass.getMappedClass(actualType);
                	MappedConstructor mappedConstructor = 
                		mappedClass.getConstructor();
                	if (mappedConstructor == null) {
                	    instance = actualType.newInstance();
                	} else {
                    	List<Object> constructorArguments = 
                    		binder.getConstructorArguments(mappedClass, subject, mappedConstructor);
                        @SuppressWarnings("unchecked")
                    	Constructor<T> constructor = (Constructor<T>) mappedConstructor.getConstructor(); 
                        instance = constructor.newInstance(constructorArguments.toArray());
                	}
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
    			} catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
    			} catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
    			}
            }
        } else {
        	throw new IllegalArgumentException("Cannot convert instance " + subject
        			+ " with types " + types + " into required type " + requiredType);
        }
        binder.bind(subject, instance);
        return instance;
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
	    return type2classes.get(uid.getId());
	}

    @SuppressWarnings("unchecked")
    public <T, N, R extends N, U extends R> Class<? extends T> matchType(Collection<R> types, Class<T> targetType,
            Dialect<N, R, ?, U, ?, ?> dialect) {
        Class<? extends T> result = targetType;
        boolean foundMatch = types.isEmpty();
        for (R type : types) {
            if (dialect.getNodeType(type) == NodeType.URI) {
                UID uid = dialect.getUID((U) type);
                List<Class<?>> classes = type2classes.get(uid.getId());
                if (classes != null) {
                    for (Class<?> clazz : classes) {
                        if ((result == null || result.isAssignableFrom(clazz)) && !clazz.isInterface()) {
                            foundMatch = true;
                            result = (Class<? extends T>) clazz;
                        }
                    }
                }
            }
        }
        if (foundMatch) {
            return result;
        } else {
            return null;
        }
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
    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
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
