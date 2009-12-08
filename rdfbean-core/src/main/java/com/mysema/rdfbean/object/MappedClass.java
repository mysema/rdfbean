/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 * 
 */
public class MappedClass {
    
//    public String test = "";

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
                path = MappedPath.getPathMapping(classNs, field, mappedClass);
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
            path = MappedPath.getPathMapping(classNs, method, mappedClass);
            if (path != null) {
                mappedClass.addMappedPath(path);
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
    
    @Nullable
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
    
    public List<MappedClass> getMappedSuperClasses() {
        Class<?> superClass = clazz.getSuperclass();
        Class<?>[] ifaces = clazz.getInterfaces();
        List<MappedClass> mappedSuperClasses = new ArrayList<MappedClass>(ifaces != null ? ifaces.length + 1 : 1);
        if (superClass != null && !Object.class.equals(superClass)) {
            if (isProcessedClass(superClass)) {
                mappedSuperClasses.add(getMappedClass(superClass));
            }
        } else {
            mappedSuperClasses.add(null);
        }
        if (ifaces != null) {
            for (Class<?> iface : ifaces) {
                if (isProcessedClass(iface)) {
                    mappedSuperClasses.add(getMappedClass(iface));
                }
            }
        }
        return mappedSuperClasses;
    }
    
    private static boolean isProcessedClass(Class<?> clazz) {
        Package pack = clazz.getPackage();
        return pack == null || !pack.getName().startsWith("java");
    }
    
    public static MappedClass getMappedClass(Class<?> clazz) {
        // NOTE: no need to further synchronize access to mappedPaths because
        // result is immutable and deterministic, i.e. it does't really matter
        // if it gets calculated multiple times
        MappedClass mappedClass = mappedClasses.get(clazz);
        if (mappedClass == null) {
            mappedClass = new MappedClass(clazz);
            if (!clazz.isEnum()) {
                for (MappedClass mappedSuperClass : mappedClass.getMappedSuperClasses()) {
                    if (mappedSuperClass != null) {
                        for (MappedPath path : mappedSuperClass.getProperties()) {
                            MappedProperty<?> property = (MappedProperty<?>) path.getMappedProperty().clone();
                            property.resolve(mappedClass);
                            mappedClass.addMappedPath(
                                    new MappedPath(property, 
                                            path.getPredicatePath(), 
                                            !mappedClass.equals(property.getDeclaringClass())));
                        }
                    }
                }
    
                // Collect direct properties (merge with super properties)
                collectFieldPaths(clazz, mappedClass);
                collectMethodPaths(clazz, mappedClass);
                assignConstructor(clazz, mappedClass);
                mappedClass.close();
                mappedClasses.put(clazz, mappedClass);
            }
        }
        return mappedClass;
    }

    private final Class<?> clazz;
    
    private final UID uid;
    
    private MappedProperty<?> idProperty;
    
    private Map<String, MappedPath> properties = new LinkedHashMap<String, MappedPath>();
    
    private MappedConstructor constructor;
    
    MappedClass(Class<?> clazz) {
        this.clazz = clazz;
        uid = getUID(clazz);
    }

    @SuppressWarnings("unchecked")
    private void addMappedPath(MappedPath path) {
        MappedProperty property = path.getMappedProperty();
        MappedPath existingPath = properties.get(property.getName());
        
        if (path.getMappedProperty().isIdReference()) {
            if (idProperty != null) {
                throw new IllegalArgumentException("Duplicate ID property: " + 
                        idProperty + " and " + path.getMappedProperty());
            }
            idProperty = path.getMappedProperty();
            properties.put(path.getName(), path);
        } 

        else if (existingPath != null) {
            MappedProperty existingProperty = existingPath.getMappedProperty();
            if (property instanceof FieldProperty) {
                if (existingProperty instanceof FieldProperty) {
                    throw new IllegalArgumentException("Cannot merge field properties: " + 
                            path + " into " + existingPath);
                } else {
                    // Field property overrides method and constructor properties
                    properties.put(path.getName(), path);
                    path.merge(existingPath);
                }
            } else {
                existingPath.merge(path);
            }
        }
        
        else {
            properties.put(path.getName(), path);
        }
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
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MappedClass) {
            return clazz.equals(((MappedClass) obj).clazz);
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return clazz.hashCode();
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

    Type resolveTypeVariable(String typeVariableName, MappedClass declaringClass) {
        int i = 0;
        for (TypeVariable<?> typeParameter : declaringClass.clazz.getTypeParameters()) {
            if (typeParameter.getName().equals(typeVariableName)) {
                break;
            } else {
                i++;
            }
        }
        int j = 0;
        boolean found = false;;
        for (MappedClass superClass : getMappedSuperClasses()) {
            if (declaringClass.equals(superClass)) {
                found = true;
                break;
            } else {
                j++;
            }
        }
        if (!found) {
            throw new RuntimeException("Super class declaration for " + declaringClass + " not found from " + this);
        }
        
        Type type = (j == 0 ? clazz.getGenericSuperclass() : clazz.getGenericInterfaces()[j-1]);
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[i];
        } else {
            throw new RuntimeException("Generic parameters not supplied from " + this + " to " + declaringClass);
        }
    }

}
