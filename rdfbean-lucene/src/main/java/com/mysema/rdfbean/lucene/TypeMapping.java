/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.Collection;
import java.util.Set;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.UID;

/**
 * TypeMapping provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface TypeMapping {

    /**
     * @param predicate
     * @param subjectTypes
     * @return
     */
    PropertyConfig findPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes);

    /**
     * @return
     */
    Set<UID> getComponentProperties();

    /**
     * @return
     */
    Set<ID> getComponentTypes();

    /**
     * Get the transitive supertypes of the given type
     * 
     * @param type
     * @return
     */
    Collection<? extends ID> getSupertypes(ID type);

    /**
     * Get the transitive subtypes of the given type
     * 
     * @param type
     * @return
     */
    Collection<? extends ID> getSubtypes(ID type);

    /**
     * @param uids
     */
    void initialize(Collection<UID> uids);

}