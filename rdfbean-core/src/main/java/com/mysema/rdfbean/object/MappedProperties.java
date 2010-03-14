/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Properties;
import com.mysema.rdfbean.model.UID;

/**
 * @author mala
 * 
 */
@Immutable
public final class MappedProperties {

    private final MappedProperty<?> mappedProperty;
    
    private final Type mappedKey;
    
    public MappedProperties(MappedProperty<?> property) {
        this.mappedProperty = property;
        this.mappedKey = property.getGenericType();
    }
    
    public MappedProperty<?> getMappedProperty() {
        return mappedProperty;
    }
    
    public String getName() {
        return mappedProperty.getName();
    }

    public boolean isWildcard() {
        return isWildcard(mappedProperty.getType());
    }
    
    public static boolean isWildcard(Class<?> type) {
        return type == null || Object.class.equals(type);
    }
    
    public boolean isClassReference() {
        return mappedProperty.isClassReference();
    }
    
    public boolean isReference() {
        return isMappedClass(mappedProperty.getTargetType()) 
            || mappedProperty.isURI()
            || mappedProperty.isInjection();
    }

    public static boolean isMappedClass(Class<?> type) {
        return type != null && type.isAnnotationPresent(ClassMapping.class);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mappedProperty.toString());
        return sb.toString();
    }

    public Type getMappedKey() {
        return mappedKey;
    }

    public static MappedProperties getMapping(String classNs, Field field, MappedClass declaringClass) {

        FieldProperty property = new FieldProperty(field, declaringClass);
        
        Properties props = property.getAnnotation(Properties.class);

        if (props != null) {
            property.resolve(null);
            
            if (!property.isMap()) {
                throw new IllegalArgumentException("Only properties type of java.util.Map, can be annotated with @Properties");
            }
            if (!UID.class.equals(property.getKeyType())) {
                throw new IllegalArgumentException("Key must be type of com.mysema.rdfbean.model.UID");
            }
            
            return new MappedProperties(property);
        }
        else {
            return null;
        }
    }
}