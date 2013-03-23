/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;

import com.mysema.rdfbean.annotations.Container;
import com.mysema.rdfbean.annotations.ContainerType;

/**
 * @author tiwe
 * 
 */
@SuppressWarnings("all")
public class ContainerImpl implements Container {

    private final ContainerType containerType;

    public ContainerImpl(ContainerType containerType) {
        this.containerType = containerType;
    }

    @Override
    public ContainerType value() {
        return containerType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Container.class;
    }

}
