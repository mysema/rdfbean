package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.config.CompassConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.DefaultConfiguration;


/**
 * LuceneConfigurationTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneConfigurationTest {
    
    private LuceneConfiguration configuration;
    
    @Before
    public void setUp(){
        configuration = new LuceneConfiguration();        
        configuration.setCompassConfig(new CompassConfiguration());
    }
    
    @ClassMapping(ns=TEST.NS)
    @Searchable(storeAll=true)
    public static class AllStored{
        
        @Predicate
        String title, description;
        
        @Predicate
        @SearchablePredicate(index=Index.NO)
        String text;
    }

    @Test
    public void allStored(){        
        configuration.setCoreConfiguration(new DefaultConfiguration(AllStored.class));
        configuration.initialize();    
        
        for (String prop : Arrays.asList("title", "description", "text")){
            PropertyConfig config = configuration.getPropertyConfig(new UID(TEST.NS, prop));
            assertNotNull(config);
            assertEquals(Store.YES, config.getStore());
            assertEquals(Index.NO, config.getIndex());
            assertFalse(config.isAllIndexed());
            assertFalse(config.isTextIndexed());
        }       
         
    }
    
    @Test
    public void defaultConfig(){
        configuration.setCoreConfiguration(new DefaultConfiguration());
        PropertyConfig defaultConfig = new PropertyConfig(Store.YES, Index.NOT_ANALYZED, false, true);
        configuration.setDefaultPropertyConfig(defaultConfig);
        
        for (String prop : Arrays.asList("title", "description", "text")){
            assertEquals(defaultConfig, configuration.getPropertyConfig(new UID(TEST.NS, prop)));
        }
    }
    
}
