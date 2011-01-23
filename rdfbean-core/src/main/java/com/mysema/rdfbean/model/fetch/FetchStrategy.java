/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model.fetch;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.UID;


/**
 * @author sasa
 *
 */
public interface FetchStrategy {

    @Nullable
    STMTMatcher getCacheKey(ID subject, UID predicate, NODE object, UID context, boolean includeInferred);

}
