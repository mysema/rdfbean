package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;

public class VirtuosoRepositoryTest {

    private Repository repository;
    
    private RDFConnection conn;

    @Before
    public void setUp(){
        repository = new VirtuosoRepository("localhost:1111", "dba", "dba");
        repository.initialize();
    }

    @After
    public void tearDown(){
        if (conn != null){
            conn.close();
        }        
        repository.close();
    }
    
    @Test
    public void Load(){
        UID foaf = new UID("http://xmlns.com/foaf/0.1/");
        conn = repository.openConnection();
        conn.remove(null, null, null, foaf);
        assertFalse(conn.exists(null, null, null, foaf, false));
        
        InputStream is = getClass().getResourceAsStream("/foaf.rdf");
        repository.load(Format.RDFXML, is, foaf, true);
        assertTrue(conn.exists(null, null, null, foaf, false));
    }
    
    @Test
    @Ignore
    public void Export() throws UnsupportedEncodingException{
        DummyOutputStream out = new DummyOutputStream();
        repository.export(Format.NTRIPLES, out);
        assertTrue(out.getLength() > 0);
    }
    
    @Test
    public void OpenConnection(){
        RDFConnection connection = repository.openConnection();
        try{
            assertNotNull(connection);
        }finally{
            connection.close();
        }
    }

}
