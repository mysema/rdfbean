/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Path;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 * 
 */
public final class MappedPath {

    @Nullable
    static MappedPath getMappedPath(MappedProperty<?> property, @Nullable List<MappedPredicate> path) {
        property.resolve(null);
        if (path != null) {
            return new MappedPath(property, path, false);
        } else {
            if (property.isAnnotatedProperty()) {
                return new MappedPath(property, Collections.<MappedPredicate>emptyList(), false);
            } else {
                return null;
            }
        }
    }

    static MappedPath getPathMapping(String classNs, Field field, MappedClass declaringClass) {
        FieldProperty property = new FieldProperty(field, declaringClass);
        List<MappedPredicate> path = getPredicatePath(classNs, property);
        return getMappedPath(property, path);
    }

    @Nullable
    static MappedPath getPathMapping(MappedClass mappedClass, Constructor<?> constructor, int parameterIndex) {
        ConstructorParameter constructorParameter = new ConstructorParameter(constructor, parameterIndex, mappedClass);
        if (constructorParameter.isPropertyReference()) {
            return mappedClass.getMappedPath(constructorParameter.getReferencedProperty());
        } else {
            List<MappedPredicate> path = getPredicatePath(mappedClass.getClassNs(), constructorParameter);
            return getMappedPath(constructorParameter, path);
        }
    }
    
    @Nullable
    static MappedPath getPathMapping(String classNs, Method method, MappedClass declaringClass) {
        MethodProperty property = MethodProperty.getMethodPropertyOrNull(method, declaringClass);
        if (property != null) {
            List<MappedPredicate> path = getPredicatePath(classNs, property);
            return getMappedPath(property, path);
        } else {
            return null;
        }
    }

    @Nullable
    private static List<MappedPredicate> getPredicatePath(String classNs, 
            MappedProperty<?> property) {
        String parentNs = classNs;
        Path path = property.getAnnotation(Path.class);
        Predicate[] predicates;
        if (path != null) {
            if (StringUtils.isNotEmpty(path.ns())) {
                parentNs = path.ns();
            }
            predicates = path.value();
        } else {
            Predicate predicate = property.getAnnotation(Predicate.class);
            if (predicate != null) {
                predicates = new Predicate[] { predicate };
            } else {
                predicates = null;
            }
        }
        if (predicates != null) {
            List<MappedPredicate> predicatePath = 
                new ArrayList<MappedPredicate>(predicates.length);
            boolean first = true;
            for (Predicate predicate : predicates) {
                predicatePath.add(
                        new MappedPredicate(parentNs, predicate, 
                                first ? property.getName() : null));
                first = false;
            }
            return predicatePath;
        } else {
            return null;
        }
    }

    private boolean ignoreInvalid;

    private List<MappedPredicate> predicatePath;
    
    private final MappedProperty<?> mappedProperty;
    
    private boolean constructorArgument;
    
    private boolean inherited = false;
    
    public MappedPath(MappedProperty<?> property, 
            List<MappedPredicate> predicatePath, 
            boolean inherited) {
        this.mappedProperty = property;
        this.predicatePath = predicatePath;
        this.inherited = inherited;
        Path path = property.getAnnotation(Path.class);
        if (path != null) {
            this.ignoreInvalid = path.ignoreInvalid();
        } else {
            if (predicatePath.size() > 0) {
                this.ignoreInvalid = predicatePath.get(0).ignoreInvalid();
            }
        }
        validate();
    }

    public MappedPredicate get(int i) {
        return predicatePath.get(i);
    }

    public MappedProperty<?> getMappedProperty() {
        return mappedProperty;
    }
    
    public String getName() {
        return mappedProperty.getName();
    }

    public List<MappedPredicate> getPredicatePath() {
        return predicatePath;
    }

    public UID getPropertyUri(int index) {
        return predicatePath.get(index).getUID();
    }

    public boolean isConstructorParameter() {
        return constructorArgument;
    }
    
    public boolean isIgnoreInvalid() {
        return ignoreInvalid;
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
    
    public boolean isInverse(int index) {
        return predicatePath.get(index).inv();
    }

    public boolean isSimpleProperty() {
        return predicatePath.size() == 1 
            && !get(0).inv() && !get(0).includeInferred();
    }

    void setConstructorArgument(boolean constructorArgument) {
        this.constructorArgument = constructorArgument;
    }

    
    void merge(MappedPath other) {
        mappedProperty.addAnnotations(other.mappedProperty);
        if (other.predicatePath != null && !other.predicatePath.isEmpty()) {
            if (this.predicatePath != null && !this.predicatePath.isEmpty()) {
                throw new IllegalArgumentException("Cannot override predicate path of " + this 
                        + " with " + other);
            }
            this.predicatePath = other.predicatePath;
        }
        this.inherited = this.inherited || other.inherited;
    }
    
    public int size() {
        return predicatePath.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mappedProperty.toString());
        sb.append(" {");
        boolean first = true;
        for (MappedPredicate predicate : predicatePath) {
            sb.append(' ');
            if (predicate.inv()) {
                sb.append('^');
            } else if (!first) {
                sb.append('.');
            }
            sb.append(predicate.getReadableURI());
            first = false;
        }
        sb.append(" }");
        return sb.toString();
    }

    public void validate() {
        Assert.notNull(mappedProperty);
        mappedProperty.validate(this);
    }

    public int getOrder() {
        if (isSimpleProperty()) {
            if (!isReference()) {
                return 1;
            } else {
                return 2;
            }
        } else if (size() == 1) {
            return 3;
        } else {
            return 4;
        }
    }

    public boolean isInherited() {
        return inherited;
    }

}
