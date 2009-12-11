/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.compass.core.config.CompassConfiguration;
import org.junit.After;
import org.junit.Before;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.object.Configuration;

/**
 * AbstractRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractLuceneTest {

    protected LuceneRepository luceneRepository;
    
    private File indexDir = new File("target/test-index");
    
    @Before
    public void setUp() throws IOException, InterruptedException{
        FileUtils.deleteDirectory(indexDir);
        CompassConfiguration compassConfig = new CompassConfiguration();
        compassConfig.configure("/compass.xml");
        
        DefaultLuceneConfiguration configuration = new DefaultLuceneConfiguration();
        configuration.setCoreConfiguration(getCoreConfiguration());
        configuration.setDefaultPropertyConfig(getDefaultPropertyConfig());
        configuration.setCompassConfig(compassConfig);
        configuration.addPrefix("test", TEST.NS);
        luceneRepository = new LuceneRepository(configuration);
    }
    
    protected abstract Configuration getCoreConfiguration();
    
    protected PropertyConfig getDefaultPropertyConfig(){
        return null;
    }

    @After
    public void tearDown() throws IOException{        
        if (luceneRepository != null){
            luceneRepository.close();    
        }
    }
}
