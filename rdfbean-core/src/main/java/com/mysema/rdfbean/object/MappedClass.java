/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;
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
    
    public static String getClassNs(Class<?> clazz) {
        ClassMapping cmap = clazz.getAnnotation(ClassMapping.class);
        if (cmap != null) {
            return cmap.ns();
        } else {
            return "";
        }
    }

    public static MappedClass getMappedClass(Class<?> clazz) {
        return MappedClassFactory.getMappedClass(clazz);
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
    
    private static boolean isProcessedClass(Class<?> clazz) {
        Package pack = clazz.getPackage();
        return pack == null || !pack.getName().startsWith("java");
    }
    
    private final Class<?> clazz;
    
    @Nullable
    private MappedConstructor constructor;
    
    @Nullable
    private MappedProperty<?> idProperty;

    private Map<String, MappedPath> properties = new LinkedHashMap<String, MappedPath>();
    
    @Nullable
    private final UID uid;
    
    MappedClass(Class<?> clazz) {
        this.clazz = clazz;
        uid = getUID(clazz);
    }
    
    @SuppressWarnings("unchecked")
    void addMappedPath(MappedPath path) {
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
    
    void close() {
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

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MappedClass) {
            return clazz.equals(((MappedClass) obj).clazz);
        } else {
            return false;
        }
    }
    
    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> atype) {
        return clazz.getAnnotation(atype);
    }
    
    public String getClassNs() {
        return getClassNs(clazz);
    }
    
    @Nullable
    public MappedConstructor getConstructor() {
        return constructor;
    }

    public MappedProperty<?> getIdProperty() {
        return idProperty;
    }

    public Class<?> getJavaClass() {
        return clazz;
    }
    
    public MappedPath getMappedPath(String name) {
        MappedPath path = properties.get(name);
        if (path != null) {
            return path;
        } else {
            throw new IllegalArgumentException("No such property: " + name + " in " + clazz);
        }
    }
    
    public List<MappedClass> getMappedSuperClasses() {
        Class<?> superClass = clazz.getSuperclass();
        Class<?>[] ifaces = clazz.getInterfaces();
        List<MappedClass> mappedSuperClasses = new ArrayList<MappedClass>(ifaces != null ? ifaces.length + 1 : 1);
        if (superClass != null && !Object.class.equals(superClass)) {
            if (isProcessedClass(superClass)) {
                mappedSuperClasses.add(getMappedClass(superClass));
            }
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
    
    public Iterable<MappedPath> getProperties() {
        return properties.values();
    }
    
    @Nullable
    public UID getUID() {
        return uid;
    }
    
    public int hashCode() {
        return clazz.hashCode();
    }

    public boolean isEnum() {
        return clazz.isEnum();
    }

    public boolean isPolymorphic() {
        return isPolymorphic(clazz);        
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

    void setMappedConstructor(MappedConstructor constructor) {
        if (constructor == null && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("Default or mapped constructor required for " + clazz);
        } else {
            this.constructor = constructor;
        }
    }

    public String toString() {
        return clazz.toString();
    }

}
