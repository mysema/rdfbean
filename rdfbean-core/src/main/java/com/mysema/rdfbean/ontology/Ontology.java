/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.ontology;

import java.util.Collection;

/**
 * Ontology provides access to type and property hierarchies
 *
 * @author tiwe
 * @version $Id$
 */
public interface Ontology<T> {
    
    /**
     * Get the transitive and recursive sub types for the given type
     * 
     * @param uid
     * @return
     */
    Collection<T> getSubtypes(T uid);
    
    /**
     * Get the transitive super types for the given type 
     * 
     * @param uid
     * @return
     */
    Collection<T> getSupertypes(T uid);
    
    /**
     * Get the transitive and recursive sub properties for the given property
     * 
     * @param uid
     * @return
     */
    Collection<T> getSubproperties(T uid);
    
    /**
     * Get the transitive super properties for the given property
     * 
     * @param uid
     * @return
     */
    Collection<T> getSuperproperties(T uid);
}
