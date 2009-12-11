package com.mysema.rdfbean.lucene;

import java.util.Collection;

import org.compass.core.config.CompassConfiguration;

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

    CompassConfiguration getCompassConfiguration();

    NodeConverter getConverter();

    Configuration getCoreConfiguration();

    PropertyConfig getPropertyConfig(UID predicate, Collection<? extends ID> subjectTypes);

    void initialize();

    boolean isContextsStored();

}