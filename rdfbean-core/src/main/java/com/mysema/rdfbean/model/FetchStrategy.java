/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import javax.annotation.Nullable;


/**
 * @author sasa
 *
 */
public interface FetchStrategy {

    @Nullable
    STMTMatcher getCacheKey(ID subject, UID predicate, NODE object, UID context, boolean includeInferred);

}
