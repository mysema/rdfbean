package com.mysema.rdfbean.jena;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;


public class JenaConnectionTest extends AbstractConnectiontest{

    @Test
    public void Update_with_nulls(){
        connection.update(Collections.<STMT>emptySet(), null);
        connection.update(null, Collections.<STMT>emptySet());
        connection.update(null, null);
    }

    @Test
    public void Exists(){
        UID context = new UID(TEST.NS);
        assertFalse(connection.exists(null, null, null, context, false));
        connection.update(null, Collections.singleton(new STMT(new BID(), RDF.type, RDFS.Class, context)));
        assertTrue(connection.exists(null, null, null, context, false));
    }
    
}
