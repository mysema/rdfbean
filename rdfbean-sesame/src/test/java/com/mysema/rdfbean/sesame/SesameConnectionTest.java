package com.mysema.rdfbean.sesame;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;


public class SesameConnectionTest {

    private Repository repository;

    private RDFConnection conn;

    @Before
    public void setUp(){
        repository = new MemoryRepository();
        repository.initialize();
        conn = repository.openConnection();
    }

    @After
    public void tearDown(){
        conn.close();
        repository.close();
    }

    @Test
    public void Update_with_nulls(){
        conn.update(Collections.<STMT>emptySet(), null);
        conn.update(null, Collections.<STMT>emptySet());
        conn.update(null, null);
    }

}
