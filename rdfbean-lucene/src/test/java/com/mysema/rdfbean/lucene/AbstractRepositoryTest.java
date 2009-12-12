package com.mysema.rdfbean.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.compass.core.config.CompassConfiguration;
import org.junit.After;
import org.junit.Before;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;

/**
 * AbstractLuceneTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractRepositoryTest {
    
    private File indexDir = new File("target/test-index");
    
    protected Repository repository;
    
    protected Repository createRepository(LuceneConfiguration configuration){
        return new LuceneRepository(configuration);
    }
    
    protected abstract Configuration getCoreConfiguration();
    
    protected PropertyConfig getDefaultPropertyConfig(){
        return null;
    }
    
    protected abstract RepositoryMode getMode();
    
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
        configuration.setMode(getMode());
        repository = createRepository(configuration);
    }

    @After
    public void tearDown() throws IOException{        
        if (repository != null){
            repository.close();    
        }
    }

}
