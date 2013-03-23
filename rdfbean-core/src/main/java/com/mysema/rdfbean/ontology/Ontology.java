/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.ontology;

import java.util.Collection;

import com.mysema.rdfbean.model.UID;

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
    Collection<UID> getSubtypes(UID uid);

    /**
     * Get the transitive super types for the given type
     * 
     * @param uid
     * @return
     */
    Collection<UID> getSupertypes(UID uid);

    /**
     * Get the transitive and recursive sub properties for the given property
     * 
     * @param uid
     * @return
     */
    Collection<UID> getSubproperties(UID uid);

    /**
     * Get the transitive super properties for the given property
     * 
     * @param uid
     * @return
     */
    Collection<UID> getSuperproperties(UID uid);
}
