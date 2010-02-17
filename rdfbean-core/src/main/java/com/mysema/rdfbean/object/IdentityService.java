/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;

/**
 * IdentityService provides ID/LID mapping functions
 * 
 * @author sasa
 *
 */
public interface IdentityService {
    
    /**
     * Get the local id for the given ID
     * 
     * @param id
     * @return
     */
    LID getLID(ID id);
    
    /**
     * Get the ID for the given local id 
     * 
     * @param lid
     * @return
     */
    @Nullable
    ID getID(LID lid);
    
}
