/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.UID;

/**
 * IdentityService provides ID/LID mapping functions
 * 
 * @author sasa
 *
 */
public interface IdentityService {
    
    /**
     * Get the local id for the given URI
     * 
     * @param id
     * @return
     */
    LID getLID(UID id);
    
    /**
     * Get the local id for the given ID in the given model
     * 
     * @param model
     * @param id
     * @return
     */
    LID getLID(@Nullable ID model, ID id);

    /**
     * Get the ID for the given local id 
     * 
     * @param lid
     * @return
     */
    @Nullable
    ID getID(LID lid);
    
}
