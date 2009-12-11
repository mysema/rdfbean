package com.mysema.rdfbean.lucene.conf;

import org.compass.core.config.CompassConfiguration;
import org.junit.Before;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.lucene.DefaultLuceneConfiguration;

/**
 * AbstractConfigurationTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class AbstractConfigurationTest {

    protected DefaultLuceneConfiguration configuration;
    
    @Before
    public void setUp(){
        configuration = new DefaultLuceneConfiguration();        
        configuration.setCompassConfig(new CompassConfiguration());
        configuration.addPrefix("test", TEST.NS);
    }
    
}
