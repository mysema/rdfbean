/*
 * Copyright (c) 2010 Mysema Ltd.
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
 * @author tiwe
 */
public interface ErrorHandler {

    <T> T createInstanceError(ID subject, Collection<ID> types, Class<T> requiredType, Exception e);

    <T> T typeMismatchError(ID subject, Collection<ID> types, Class<T> requiredType);

    Object conversionError(NODE value, Class<?> targetType, MappedPath propertyPath, Exception e);

    void functionalValueError(ID subject, UID predicate, boolean includeInferred, UID context);

    void cardinalityError(MappedPath propertyPath, Set<? extends NODE> values);

    void namespaceMismatch(String expected, String given);

}