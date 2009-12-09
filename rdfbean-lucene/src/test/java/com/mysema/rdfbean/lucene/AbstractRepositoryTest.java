package com.mysema.rdfbean.lucene;

import java.io.File;
import java.util.UUID;

import org.compass.core.config.CompassConfiguration;
import org.junit.After;
import org.junit.Before;

/**
 * AbstractRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractRepositoryTest {

    protected LuceneRepository luceneRepository;
    
    @Before
    public void setUp(){
        new File("target/test-index").renameTo(new File("target/" + UUID.randomUUID()));
        
        CompassConfiguration compassConfig = new CompassConfiguration();
        compassConfig.configure("/compass.xml");
        
        LuceneConfiguration configuration = new LuceneConfiguration();
        configuration.setCompassConfig(compassConfig);
        luceneRepository = new LuceneRepository(configuration);
        luceneRepository.initialize();
    }
    
    @After
    public void tearDown(){
//        luceneRepository.close();
    }
}
