/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;

import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.IDType;

/**
 * @author tiwe
 *
 */
public class IdImpl implements Id{

    private final IDType idType;
    
    public IdImpl(IDType idType){
        this.idType = idType;
    }
    
    @Override
    public IDType value() {
        return idType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Id.class;
    }

}
