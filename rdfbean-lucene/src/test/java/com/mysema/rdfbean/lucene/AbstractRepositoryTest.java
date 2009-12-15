/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.compass.core.CompassHits;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.junit.After;
import org.junit.Before;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.Session;

/**
 * AbstractLuceneTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractRepositoryTest {
    
    private File indexDir = new File("target/test-index");
    
    protected Repository repository;
    
    protected Session session;
    
    protected Repository createRepository(LuceneConfiguration configuration){
        return new LuceneRepository(configuration);
    }
    
    protected void displayAndClose(CompassHits hits) {
        try{
            for (int i = 0; i < hits.length(); i++){
                Resource resource = hits.resource(i);
                for (Property property : resource.getProperties()){
                    System.out.println(property.getName() + " = " + property.getStringValue());
                }
            }                
        }finally{
            hits.close();
        }               
    }
    
    protected abstract Configuration getCoreConfiguration();
    
    protected PropertyConfig getDefaultPropertyConfig(){
        return null;
    }
    
    @Before
    public void setUp() throws IOException, InterruptedException{
        FileUtils.deleteDirectory(indexDir);
        CompassConfiguration compassConfig = new CompassConfiguration();
        compassConfig.configure("/compass.xml");
        
        DefaultLuceneConfiguration configuration = new DefaultLuceneConfiguration();
        configuration.setCoreConfiguration(getCoreConfiguration());
        configuration.setDefaultPropertyConfig(getDefaultPropertyConfig());
        configuration.setCompassConfig(compassConfig);
        configuration.addPrefix("", TEST.NS);
        repository = createRepository(configuration);
    }

    @After
    public void tearDown() throws IOException{
        try{
            if (session != null){
                session.close();
            }    
        }finally{
            if (repository != null){
                repository.close();    
            }    
        }                
    }

}
