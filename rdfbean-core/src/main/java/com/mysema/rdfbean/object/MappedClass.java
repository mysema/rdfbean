/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
public final class MappedClass {

    private static final Comparator<MappedPath> mappedPathComparator = new Comparator<MappedPath>() {
        @Override
        public int compare(MappedPath o1, MappedPath o2) {
            return o1.getOrder() - o2.getOrder();
        }
    };

    private final Class<?> clazz;

    @Nullable
    private MappedConstructor constructor;

    private final Set<MappedProperty<?>> dynamicProperties = new LinkedHashSet<MappedProperty<?>>();

    @Nullable
    private MappedProperty<?> idProperty;

    private final Set<UID> mappedPredicates = new HashSet<UID>();

    private final Set<UID> invMappedPredicates = new HashSet<UID>();

    private Map<String, MappedPath> properties = new LinkedHashMap<String, MappedPath>();

    private final List<MappedClass> mappedSuperClasses;

    @Nullable
    private final UID uid;

    @Nullable
    private final UID context;

    MappedClass(Class<?> clazz, @Nullable UID uid, @Nullable UID context, List<MappedClass> mappedSuperClasses) {
        this.clazz = Assert.notNull(clazz,"clazz");
        this.uid = uid;
        this.context = context;
        this.mappedSuperClasses = mappedSuperClasses;
        mappedPredicates.add(RDF.type);
    }

    public static String getClassNs(Class<?> clazz) {
        ClassMapping cmap = clazz.getAnnotation(ClassMapping.class);
        if (cmap != null) {
            return cmap.ns();
        } else {
            return "";
        }
    }

    void addDynamicProperty(MappedProperty<?> property) {
        // FIXME How to handle mapped keys from superclass?
        dynamicProperties.add(property);
    }

    @SuppressWarnings("unchecked")
    void addMappedPath(MappedPath path) {
        if (path.getPredicatePath().size() > 0) {
            if (!path.get(0).inv()){
                mappedPredicates.add(path.get(0).getUID());
            }else{
                invMappedPredicates.add(path.get(0).getUID());
            }
        }

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
        Arrays.sort(paths, mappedPathComparator);
        // Rebuild properties map using bind ordering
        properties = new LinkedHashMap<String, MappedPath>();
        for (MappedPath path : paths) {
            properties.put(path.getName(), path);
        }
        // Close properties map from further changes
        properties = Collections.unmodifiableMap(properties);
    }

    @Override
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
        return uid != null ? uid.ns() : "";
    }

    @Nullable
    public MappedConstructor getConstructor() {
        return constructor;
    }

    public Collection<MappedProperty<?>> getDynamicProperties() {
        return dynamicProperties;
    }

    @Nullable
    public MappedProperty<?> getIdProperty() {
        return idProperty;
    }

    public Class<?> getJavaClass() {
        return clazz;
    }
    
    public boolean hasProperty(String name){
        return properties.containsKey(name);
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
        return mappedSuperClasses;
    }

    public Collection<MappedPath> getProperties() {
        return properties.values();
    }

    @Nullable
    public UID getUID() {
        return uid;
    }

    @Nullable
    public UID getContext() {
        return context;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    public boolean isEnum() {
        return clazz.isEnum();
    }

    public Collection<UID> getMappedPredicates(){
        return mappedPredicates;
    }

    public Collection<UID> getInvMappedPredicates(){
        return invMappedPredicates;
    }

    public boolean isMappedPredicate(UID predicate) {
        return mappedPredicates.contains(predicate);
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
        boolean found = false;
        for (MappedClass superClass : getMappedSuperClasses()) {
            if (declaringClass.equals(superClass)) {
                found = true;
                break;
            } else {
                j++;
            }
        }
        if (!found) {
            throw new SessionException("Super class declaration for " + declaringClass + " not found from " + this);
        }

        Type type = (j == 0 ? clazz.getGenericSuperclass() : clazz.getGenericInterfaces()[j-1]);
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[i];
        } else {
            throw new SessionException("Generic parameters not supplied from " + this + " to " + declaringClass);
        }
    }

    void setMappedConstructor(@Nullable MappedConstructor constructor) {
        if (constructor == null && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("Default or mapped constructor required for " + clazz);
        } else {
            this.constructor = constructor;
        }
    }

    @Override
    public String toString() {
        return clazz.toString();
    }

}
