/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.lucene.PropertyConfig;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.DefaultConfiguration;

/**
 * DefaultConfigTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DefaultPropertyConfigTest extends AbstractConfigurationTest{
    
    @Test
    public void defaultConfig(){
        configuration.setCoreConfiguration(new DefaultConfiguration());
        PropertyConfig defaultConfig = new PropertyConfig(Store.YES, Index.NOT_ANALYZED, false, true, 1.0f);
        configuration.setDefaultPropertyConfig(defaultConfig);
        configuration.initialize();
        
        for (String prop : Arrays.asList("title", "description", "text")){
            assertEquals(defaultConfig, configuration.getPropertyConfig(new UID(TEST.NS, prop), 
                    Collections.singleton(RDFS.Resource)));
        }
    }

}
