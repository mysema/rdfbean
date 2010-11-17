package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;


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

    @Test
    public void Exists(){
        UID context = new UID(TEST.NS);
        assertFalse(conn.exists(null, null, null, context, false));
        conn.update(null, Collections.singleton(new STMT(new BID(), RDF.type, RDFS.Class, context)));
        assertTrue(conn.exists(null, null, null, context, false));
    }

}
