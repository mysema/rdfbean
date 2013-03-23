/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.util.List;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Path;

/**
 * @author sasa
 * 
 */
public final class MappedPath {

    private final boolean ignoreInvalid;

    private final MappedProperty<?> mappedProperty;

    private List<MappedPredicate> predicatePath;

    private boolean constructorArgument;

    private boolean inherited;

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
            } else {
                this.ignoreInvalid = false;
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

    @Deprecated
    private static boolean isMappedClass(Class<?> type) {
        return type != null && type.isAnnotationPresent(ClassMapping.class);
    }

    public boolean isInverse(int index) {
        return predicatePath.get(index).inv();
    }

    public boolean isSimpleProperty() {
        return predicatePath.size() == 1 && !get(0).inv() && !get(0).includeInferred();
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

    @Override
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
            // sb.append(predicate.getReadableURI());
            sb.append(predicate.toString());
            first = false;
        }
        sb.append(" }");
        return sb.toString();
    }

    public void validate() {
        Assert.notNull(mappedProperty, "mappedProperty");
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
