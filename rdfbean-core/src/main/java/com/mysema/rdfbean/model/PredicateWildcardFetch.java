/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;


/**
 * @author sasa
 *
 */
public class PredicateWildcardFetch implements FetchStrategy {
    
    private boolean includeInferred;

    @Override
    public STMTMatcher getCacheKey(ID subject, UID predicate, NODE object,
            UID context, boolean includeInferred) {
        if (subject != null && (this.includeInferred || !includeInferred)) {
            return new STMTMatcher(subject, null, null, context, Boolean.valueOf(includeInferred));
        } else {
            return null;
        }
    }

}
