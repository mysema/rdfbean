/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.store;

import static com.mysema.rdfbean.lucene.RDFTestData.objects;
import static com.mysema.rdfbean.lucene.RDFTestData.predicates;
import static com.mysema.rdfbean.lucene.RDFTestData.subjects;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.compass.core.config.CompassConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.lucene.DefaultLuceneConfiguration;
import com.mysema.rdfbean.lucene.LuceneEnhancedRepository;
import com.mysema.rdfbean.lucene.PropertyConfig;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.DefaultConfiguration;

/**
 * LuceneEnhancedRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneEnhancedRepositoryTest {
    
    private File indexDir = new File("target/test-index");

    private LuceneEnhancedRepository repository;
    
    @Before
    public void setUp() throws IOException{
        FileUtils.deleteDirectory(indexDir);
        CompassConfiguration compassConfig = new CompassConfiguration();
        compassConfig.configure("/compass.xml");
        
        DefaultLuceneConfiguration configuration = new DefaultLuceneConfiguration();
        configuration.setCoreConfiguration(new DefaultConfiguration());
        configuration.setDefaultPropertyConfig(new PropertyConfig(Store.YES, Index.NOT_ANALYZED, false, true, false,  1.0f));
        configuration.setCompassConfig(compassConfig);
        configuration.addPrefix("test", TEST.NS);
        
        repository = new LuceneEnhancedRepository();
        repository.setConfiguration(configuration);
        repository.setRepository(new MiniRepository());
        repository.initialize();
    }
    
    @After
    public void tearDown(){
        repository.close();
    }
    
    @Test
    public void test() throws IOException{
        RDFConnection connection = repository.openConnection();
        
        Set<STMT> added = new HashSet<STMT>();
        for (UID subject : subjects){
            for (UID predicate : predicates){
                for (NODE object : objects){
                    added.add(new STMT(subject, predicate, object));
                }
            }
        }
        connection.update(Collections.<STMT>emptySet(), added);
        
        // TODO
        
        connection.close();
    }
}
