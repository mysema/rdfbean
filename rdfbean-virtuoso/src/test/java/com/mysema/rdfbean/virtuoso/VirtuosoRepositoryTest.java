package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.NTriplesUtil;

public class VirtuosoRepositoryTest extends AbstractConnectionTest{
    
    @Test
    public void Load() throws UnsupportedEncodingException{
        STMT stmt = new STMT(new BID(), RDF.type, new BID());
        toBeRemoved = Collections.singleton(stmt);
        String ntriples = NTriplesUtil.toString(stmt);
        InputStream is = new ByteArrayInputStream(ntriples.getBytes("US-ASCII"));        
        repository.load(Format.NTRIPLES, is, context2, false);
        
        // blank node ids are not preserved in load
        assertFalse(connection.exists(stmt.getSubject(), null, null, context2, false));
        assertFalse(connection.exists(null, null, stmt.getObject(), context2, false));
    }
    
    @Test
    public void Load_RDF_XML(){
        connection.remove(null, null, null, context2);
        assertFalse(connection.exists(null, null, null, context2, false));
        
        InputStream is = getClass().getResourceAsStream("/foaf.rdf");
        repository.load(Format.RDFXML, is, context2, true);
        assertTrue(connection.exists(null, null, null, context2, false));
    }

    @Test
    public void Load_Turtle(){
        connection.remove(null, null, null, context2);
        assertFalse(connection.exists(null, null, null, context2, false));
        
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        repository.load(Format.TURTLE, is, context2, true);
        assertTrue(connection.exists(null, null, null, context2, false));
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
