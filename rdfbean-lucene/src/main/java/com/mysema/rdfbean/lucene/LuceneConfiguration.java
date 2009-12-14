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

    Compass getCompass();
    
    Set<UID> getComponentProperties();

    Set<ID> getComponentTypes();

    NodeConverter getConverter();
    
    Configuration getCoreConfiguration();

    PropertyConfig getPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes);

    void initialize();

    boolean isContextsStored();
    
    boolean isEmbeddedIds();
    
    boolean isLocalNameAsText();

}