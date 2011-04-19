package com.mysema.rdfbean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;

public abstract class AbstractConnectionTest {
    
    @BeforeClass
    public static void beforeClass(){
        Helper.helper.newRepository();
    }
    
    @AfterClass
    public static void afterClass(){
        Helper.helper.closeRepository();
    }
    
    @Before
    public void before(){
        Helper.helper.newConnection();
    }

    @After
    public void after(){
        Helper.helper.closeConnection();
    }
    
    protected RDFConnection connection(){
        return Helper.helper.connection;
    }
    
    protected Repository repository(){
        return Helper.helper.repository;
    }    
    
}
