/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

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
    
    LID getLID(UID id);
    
    LID getLID(ID model, ID id);

    ID getID(LID lid);
    
}
