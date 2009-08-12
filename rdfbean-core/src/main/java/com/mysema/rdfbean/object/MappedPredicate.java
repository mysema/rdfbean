/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

import com.mysema.rdfbean.annotations.Predicate;

/**
 * @author sasa
 *
 */
public class MappedPredicate extends URIMapping {

    private boolean inv;
    
    private boolean ignoreInvalid;
    
    private boolean includeInferred;
    
    @Nullable
    private String context;
    
    public MappedPredicate(String parentNs, Predicate predicate, @Nullable String elementName) {
        super(parentNs, predicate.ns(), predicate.ln(), elementName);
        this.inv = predicate.inv();
        this.ignoreInvalid = predicate.ignoreInvalid();
        this.includeInferred = predicate.includeInferred();
        this.context = predicate.context();
        if (this.context.length() == 0) {
            this.context = null;
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
