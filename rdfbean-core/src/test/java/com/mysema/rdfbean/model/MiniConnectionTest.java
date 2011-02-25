package com.mysema.rdfbean.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

}
