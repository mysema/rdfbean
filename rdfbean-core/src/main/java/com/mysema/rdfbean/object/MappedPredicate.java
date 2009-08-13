/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.annotations.Predicate;

/**
 * @author sasa
 *
 */
@Immutable
public class MappedPredicate extends URIMapping {

    private final boolean inv;
    
    private final boolean ignoreInvalid;
    
    private final boolean includeInferred;
    
    @Nullable
    private final String context;
    
    public MappedPredicate(String parentNs, Predicate predicate, @Nullable String elementName) {
        super(parentNs, predicate.ns(), predicate.ln(), elementName);
        this.inv = predicate.inv();
        this.ignoreInvalid = predicate.ignoreInvalid();
        this.includeInferred = predicate.includeInferred();
        if (predicate.context().isEmpty()){
            this.context = null;
        }else{
            this.context = predicate.context();
        }        
    }
    
    public boolean inv() {
        return inv;
    }
    
    public boolean ignoreInvalid() {
        return ignoreInvalid;
    }

    public boolean includeInferred() {
        return includeInferred;
    }
    
    public String context() {
        return context;
    }

}
