package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.io.Format;

public class VirtuosoRepositoryTest extends AbstractConnectionTest{
    
    private RDFConnection conn;

    @After
    public void tearDown(){
        if (conn != null){
            conn.close();
        }
        super.tearDown();
    }
    
    @Test
    public void Load(){
        conn = repository.openConnection();
        conn.remove(null, null, null, context2);
        assertFalse(conn.exists(null, null, null, context2, false));
        
        InputStream is = getClass().getResourceAsStream("/foaf.rdf");
        repository.load(Format.RDFXML, is, context2, true);
        assertTrue(conn.exists(null, null, null, context2, false));
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
