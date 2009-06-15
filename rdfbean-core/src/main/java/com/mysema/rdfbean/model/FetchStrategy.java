/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;


/**
 * @author sasa
 *
 */
public interface FetchStrategy {

    STMTMatcher getCacheKey(ID subject, UID predicate, NODE object, UID context,
            boolean includeInferred);

}
