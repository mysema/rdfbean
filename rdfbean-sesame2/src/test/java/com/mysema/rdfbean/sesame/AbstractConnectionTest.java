package com.mysema.rdfbean.sesame;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;

public abstract class AbstractConnectionTest {

    private static MemoryRepository repository;
    
    protected RDFConnection connection;
    
    @BeforeClass
    public static void beforeClass() throws IOException{
        repository = new MemoryRepository();
        repository.setSources(
                new RDFSource("classpath:/test.ttl", Format.TURTLE, TEST.NS),
                new RDFSource("classpath:/foaf.rdf", Format.RDFXML, FOAF.NS)
        );
        repository.initialize();
    
    }
    
    @AfterClass
    public static void afterClass(){
        if (repository != null) {
            repository.close();
        }
    }
    
    @Before
    public void before(){
        connection = repository.openConnection();
    }
    
    @After
    public void after(){
        if (connection != null) {
            connection.close();
        }
    }
}
