package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;

public class NativeRepositoryBlankNodeTest {

    private NativeRepository repository;

    @Before
    public void setUp(){
        repository = new NativeRepository();
        repository.setDataDirName("target/NativeRepositoryBlankNodeTest");
        repository.initialize();
    }

    @After
    public void tearDown(){
        repository.close();
    }

    @Test
    public void IsPreserved(){
        RDFConnection connection = repository.openConnection();
        try{
            STMT stmt = new STMT(connection.createBNode(), RDF.type, RDFS.Resource);
            connection.update(null, Collections.singleton(stmt));
            assertTrue(connection.exists(stmt.getSubject(), null, null, null, false));
            connection.update(Collections.singleton(stmt), null);
            assertFalse(connection.exists(stmt.getSubject(), null, null, null, false));
        }finally{
            connection.close();
        }
    }

}
