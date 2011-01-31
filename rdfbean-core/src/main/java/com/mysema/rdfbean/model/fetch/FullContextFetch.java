/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model.fetch;

import java.util.Set;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
public class FullContextFetch implements FetchStrategy {
    
    @Nullable
    private Set<UID> contexts = null;

    @Override
    public STMTMatcher getCacheKey(ID subject, UID predicate, NODE object,
            UID context, boolean includeInferred) {
        // NOTE: Inferred statements don't have reliable context information
        if (!includeInferred && (contexts == null || contexts.contains(context))) {
            return new STMTMatcher(null, null, null, context, Boolean.FALSE);
        } else {
            return null;
        }
    }

    public void setContexts(Set<UID> contexts) {
        this.contexts = contexts;
    }

}