/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Collection;
import java.util.Set;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.UID;

/**
 * Default implementation of the ErrorHandler interface
 * 
 * @author sasa
 *
 */
public class DefaultErrorHandler implements ErrorHandler {

    public <T> T createInstanceError(ID subject, Collection<ID> types, Class<T> requiredType, Exception e) {
        throw new IllegalArgumentException("Cannot create an instance of " + requiredType + " from " + subject + " with types " + types, e);
    }

    public <T> T typeMismatchError(ID subject, Collection<ID> types, Class<T> requiredType) {
        throw new IllegalArgumentException("Cannot convert instance " + subject
                + " with types " + types + " into required type " + requiredType);
    }

    public Object conversionError(NODE value, Class<?> targetType, MappedPath propertyPath, Exception e) {
        throw new IllegalArgumentException("Cannot assign " + value + " into " + propertyPath, e);
    }

    public void functionalValueError(ID subject, UID predicate,
            boolean includeInferred, UID context) {
        throw new IllegalArgumentException("Found multiple values for a functional predicate: "
                        + predicate + " of resource" + subject);
    }

    public void cardinalityError(MappedPath propertyPath, Set<? extends NODE> values) {
        throw new IllegalArgumentException(
                "Cannot assign multiple values into singleton property "
                        + propertyPath + ": " + values);
    }
    
}
