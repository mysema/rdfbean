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
@SuppressWarnings("all")
public class IdImpl implements Id {

    private final IDType idType;
    
    private final String ns;
    
    public IdImpl(IDType idType) {
        this(idType, "");
    }
    
    public IdImpl(IDType idType, String ns) {
        this.idType = idType;
        this.ns = ns;
    }
    
    @Override
    public IDType value() {
        return idType;
    }
    
       
    @Override
    public String ns() {
        return ns;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Id.class;
    }

}
