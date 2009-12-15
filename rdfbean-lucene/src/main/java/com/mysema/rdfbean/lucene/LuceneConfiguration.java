/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.Collection;
import java.util.Set;

import org.compass.core.Compass;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;

/**
 * LuceneConfiguration provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface LuceneConfiguration {

    /**
     * @return
     */
    Compass getCompass();
    
    /**
     * @return
     */
    Set<UID> getComponentProperties();

    /**
     * @return
     */
    Set<ID> getComponentTypes();

    /**
     * @return
     */
    NodeConverter getConverter();
    
    /**
     * @return
     */
    Configuration getCoreConfiguration();

    /**
     * @param predicate
     * @param subjectTypes
     * @return
     */
    PropertyConfig getPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes);

    /**
     * @param type
     * @return
     */
    Collection<? extends ID> getSupertypes(ID type);
    
    /**
     * @param type
     * @return
     */
    Collection<? extends ID> getSubtypes(ID type);
    
    /**
     * 
     */
    void initialize();

    /**
     * @return
     */
    boolean isContextsStored();
    
    /**
     * @return
     */
    boolean isEmbeddedIds();
    
    /**
     * @return
     */
    boolean isLocalNameAsText();

    /**
     * @return
     */
    boolean isIndexSupertypes();

}