package com.mysema.rdfbean.virtuoso;

import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;

public abstract class AbstractConnectionTest {

    protected static Repository repository;

    protected RDFConnection connection;
    
    protected Collection<STMT> toBeRemoved;

    @BeforeClass
    public static void setUpClass(){
        repository = new VirtuosoRepository("localhost:1111", "dba", "dba");
        repository.initialize();
    }

    @AfterClass
    public static void tearDownClass(){
        repository.close();
    }

    @Before
    public void setUp(){
        connection = repository.openConnection();
    }

    @After
    public void tearDown(){
        if (connection != null){
            if (toBeRemoved != null){
                connection.update(toBeRemoved, null);
            }            
            connection.close();
        }
    }

}
