/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import com.mysema.commons.lang.Assert;

/**
 * @author sasa
 *
 */
@Immutable
public final class MappedConstructor {

    private final Constructor<?> constructor;
    
    private final List<MappedPath> mappedArguments;
    
    public MappedConstructor(Constructor<?> constructor) {
        this(constructor, Collections.<MappedPath>emptyList());
    }

    public MappedConstructor(Constructor<?> constructor,
            List<MappedPath> mappedArguments) {
        this.constructor = Assert.notNull(constructor);
        this.mappedArguments = Assert.notNull(mappedArguments);
        this.constructor.setAccessible(true);
        for (MappedPath path : mappedArguments) {
            path.setConstructorArgument(true);
        }
    }

    public List<MappedPath> getMappedArguments() {
        return mappedArguments;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public int getArgumentCount() {
        return mappedArguments.size();
    }
    
    public String toString() {
        return constructor.toString();
    }

    public Class<?> getDeclaringClass() {
        return constructor.getDeclaringClass();
    }
}
