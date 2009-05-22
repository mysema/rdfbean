/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 * 
 */
public class MappedClass {

    private static Map<Class<?>, MappedClass> mappedClasses = Collections
            .synchronizedMap(new LinkedHashMap<Class<?>, MappedClass>());

    private static void assignConstructor(Class<?> clazz, MappedClass mappedClass) {
    	Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    	if (constructors.length == 0) {
    	    return;
    	}
    	MappedConstructor defaultConstructor = null;
    	MappedConstructor mappedConstructor = null;
    	nextConstructor:
    	for (Constructor<?> constructor : constructors) {
    		if (constructor.getParameterTypes().length == 0) {
    			defaultConstructor = new MappedConstructor(constructor);
    		} else {
    			List<MappedPath> mappedArguments = new ArrayList<MappedPath>();
    			for (int i=0; i < constructor.getParameterTypes().length; i++) {
					MappedPath mappedPath = MappedPath.getPathMapping(mappedClass, constructor, i);
					if (mappedPath != null) {
						mappedArguments.add(mappedPath);
					} else if (mappedArguments.size() > 0) {
						throw new IllegalArgumentException("Constructor has unmapped parameters: " + constructor);
					} else {
						continue nextConstructor;
					}
    			}
    			if (!mappedArguments.isEmpty()) {
    				if (mappedConstructor != null) {
    					throw new IllegalArgumentException("Ambiguous mapped constructor: " + constructor);
    				} else {
    					mappedConstructor = new MappedConstructor(constructor, mappedArguments);
    				}
    			}
    		}
    	}
    	if (mappedConstructor != null) {
    		mappedClass.setMappedConstructor(mappedConstructor);
    	} else if (defaultConstructor != null) {
    		mappedClass.setMappedConstructor(defaultConstructor);
    	} else {
    	    // TODO return false? 
    	}
	}

    private static void collectFieldPaths(Class<?> clazz, MappedClass mappedClass) {
        if (!clazz.isInterface()) {
            MappedPath path;
            String classNs = MappedClass.getClassNs(clazz);
            for (Field field : clazz.getDeclaredFields()) {
                path = MappedPath.getPathMapping(classNs, field);
                if (path != null) {
                    mappedClass.addMappedPath(path);
                }
            }
        }
    }

    private static void collectMethodPaths(Class<?> clazz, MappedClass mappedClass) {
        MappedPath path;
        String classNs = MappedClass.getClassNs(clazz);
        for (Method method : clazz.getDeclaredMethods()) {
            path = MappedPath.getPathMapping(classNs, method);
            if (path != null) {
                mappedClass.mergeMappedPath(path);
            }
        }
    }

    public static String getClassNs(Class<?> clazz) {
        ClassMapping cmap = clazz.getAnnotation(ClassMapping.class);
        if (cmap != null) {
            return cmap.ns();
        } else {
            return "";
        }
    }
    
    public String getClassNs() {
    	return getClassNs(clazz);
    }
    
    public static UID getUID(Class<?> clazz) {
    	ClassMapping cmap = clazz.getAnnotation(ClassMapping.class);
        if (cmap != null) {
            if (StringUtils.isNotEmpty(cmap.ln())) {
                return new UID(cmap.ns(), cmap.ln());
            } else if (StringUtils.isNotEmpty(cmap.ns())) {
                return new UID(cmap.ns(), clazz.getSimpleName());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean isPolymorphic(Class<?> clazz) {
        // TODO use configuration to check if there's any mapped subclasses 
        return !Modifier.isFinal(clazz.getModifiers());
    }
    
    public static MappedClass getMappedClass(Class<?> clazz) {
        // NOTE: no need to further synchronize access to mappedPaths because
        // result is immutable and deterministic, i.e. it does't really matter
        // if it gets calculated multiple times
        MappedClass mappedClass = mappedClasses.get(clazz);
        if (mappedClass == null) {
            mappedClass = new MappedClass(clazz);
            
            // Collect super class properties 
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                MappedClass mappedSuperClass = getMappedClass(superClass);
                for (MappedPath path : mappedSuperClass.getProperties()) {
                    mappedClass.addMappedPath(new MappedPath(path.getMappedProperty(), 
                            path.getPredicatePath(), true));
                }
            }
            
            // Collect interface properties (me'rge with super class properties)
            for (Class<?> iface : clazz.getInterfaces()) {
                MappedClass mappedInterface = getMappedClass(iface);
                for (MappedPath path : mappedInterface.getProperties()) {
                    mappedClass.mergeMappedPath(new MappedPath(path.getMappedProperty(), 
                            path.getPredicatePath(), true));
                }
            }

            // Collect direct properties (merge with super properties)
            collectFieldPaths(clazz, mappedClass);
            collectMethodPaths(clazz, mappedClass);
            assignConstructor(clazz, mappedClass);
            mappedClass.close();
            mappedClasses.put(clazz, mappedClass);
        }
        return mappedClass;
    }

	private Class<?> clazz;
    
    private UID uid;
    
    private MappedProperty<?> idProperty;
    
    private Map<String, MappedPath> properties = new LinkedHashMap<String, MappedPath>();
    
    private MappedConstructor constructor;
    
    MappedClass(Class<?> clazz) {
        this.clazz = clazz;
        uid = getUID(clazz);
    }

    private void addMappedPath(MappedPath path) {
        addMappedPath(path, false);
    }

    private void addMappedPath(MappedPath path, boolean merge) {
        MappedProperty<?> property = path.getMappedProperty();
        MappedPath existingPath = properties.get(property.getName());

        if (!merge && existingPath != null) {
            throw new IllegalArgumentException("Duplicate property: " + path + " and ");
        }
        
        else if (path.getMappedProperty().isIdReference()) {
            if (idProperty != null) {
                throw new IllegalArgumentException("Duplicate ID property: " + 
                        idProperty + " and " + path.getMappedProperty());
            }
            idProperty = path.getMappedProperty();
            properties.put(path.getName(), path);
        } 

        else if (existingPath != null) {
            existingPath.getMappedProperty().addAnnotations(property);
        } 
        
        else {
            properties.put(path.getName(), path);
        }
    }
    
    private void mergeMappedPath(MappedPath path) {
        addMappedPath(path, true);
    }
    
	private void close() {
	    MappedPath[] paths = properties.values().toArray(new MappedPath[properties.size()]);
	    // Sort properties into bind order
	    Arrays.sort(paths, new Comparator<MappedPath>() {
            @Override
            public int compare(MappedPath o1, MappedPath o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
	    // Rebuild properties map using bind ordering
        properties = new LinkedHashMap<String, MappedPath>();
        for (MappedPath path : paths) {
            properties.put(path.getName(), path);
        }
        // Close properties map from further changes
        properties = Collections.unmodifiableMap(properties);
    }
    
    public MappedConstructor getConstructor() {
		return constructor;
	}

    public MappedProperty<?> getIdProperty() {
        return idProperty;
    }

    public MappedPath getMappedPath(String name) {
        MappedPath path = properties.get(name);
        if (path != null) {
            return path;
        } else {
            throw new IllegalArgumentException("No such property: " + name + " in " + clazz);
        }
    }
    
    public Class<?> getJavaClass() {
        return clazz;
    }
    
    public UID getUID() {
        return uid;
    }
    
    public Iterable<MappedPath> getProperties() {
        return properties.values();
    }

    public boolean isPolymorphic() {
        return isPolymorphic(clazz);
    }

    private void setMappedConstructor(MappedConstructor constructor) {
		if (constructor == null && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
			throw new IllegalArgumentException("Default or mapped constructor required for " + clazz);
		} else {
			this.constructor = constructor;
		}
    }
    
    public String toString() {
    	return clazz.toString();
    }

    public boolean isEnum() {
        return clazz.isEnum();
    }

}
