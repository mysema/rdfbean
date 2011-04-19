package com.mysema.rdfbean.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class MiniConnectionTest {

    @Test
    public void Update_with_nulls(){
        RDFConnection conn = new MiniRepository().openConnection();
        conn.update(Collections.<STMT>emptySet(), null);
        conn.update(null, Collections.<STMT>emptySet());
        conn.update(null, null);
    }

    @Test
    public void Remove(){
        MiniConnection conn = new MiniRepository().openConnection();
        conn.addStatements(new STMT(new BID(), RDF.type, RDFS.Resource));
        assertTrue(conn.exists(null, null, null, null, false));
        conn.remove(null, null, null, null);
        assertFalse(conn.exists(null, null, null, null, false));
    }
    
    @Test
    public void Remove_With_Context(){
        MiniConnection conn = new MiniRepository().openConnection();
        STMT stmt1 = new STMT(RDF.type, RDF.type, RDF.type);
        STMT stmt2 = new STMT(RDF.type, RDF.type, RDF.type, RDF.type);
        conn.update(null, Arrays.asList(stmt1, stmt2));
        conn.update(Collections.singleton(stmt2), null);
        
        assertFalse(conn.exists(null, null, null, RDF.type, false));
        assertTrue(conn.exists(RDF.type, RDF.type, RDF.type, null, false));
        
    }

}
