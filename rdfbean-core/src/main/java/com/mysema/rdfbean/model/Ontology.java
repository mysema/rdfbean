/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Set;

/**
 * Ontology provides access to type and property hierarchies
 *
 * @author tiwe
 * @version $Id$
 */
public interface Ontology {
    
    /**
     * Get the transitive and recursive sub types for the given type
     * 
     * @param uid
     * @return
     */
    Set<UID> getSubtypes(UID uid);
    
    /**
     * Get the transitive super types for the given type 
     * 
     * @param uid
     * @return
     */
    Set<UID> getSupertypes(UID uid);
}
