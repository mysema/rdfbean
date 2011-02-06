/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.*;

/**
 * RDBRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBRepositoryTest extends AbstractRDBTest{
    
    private Operation<Long> countOp = new CountOperation(); 
    
    @Test
    public void Execute() {
        repository.execute(countOp);
    }

    @Test
    public void Export() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, null, baos);
    }

    @Test
    public void Load_withContext_replace(){
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        UID context = new UID(TEST.NS);
        repository.load(Format.TURTLE, is, context, true);
        long count1 = repository.execute(countOp);
        repository.execute(new Addition(new STMT(new BID(), RDF.type, RDFS.Resource, context)));
        
        // reload with replace
        is = getClass().getResourceAsStream("/test.ttl");
        repository.load(Format.TURTLE, is, context, true);
        assertEquals(count1, repository.execute(countOp).longValue());
    }
    
    @Test
    public void Load_withContext_withoutReplace(){
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        UID context = new UID(TEST.NS);
        repository.load(Format.TURTLE, is, context, false);
        long count1 = repository.execute(countOp);
        repository.execute(new Addition(new STMT(new BID(), RDF.type, RDFS.Resource, context)));
        
        // reload without replace
        is = getClass().getResourceAsStream("/test.ttl");
        repository.load(Format.TURTLE, is, context, false);
        assertEquals(count1 + 1, repository.execute(countOp).longValue());
    }
    
    @Test
    public void Load_withoutContext(){
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        repository.load(Format.TURTLE, is, null, false);
        assertTrue(repository.execute(countOp) > 0);
    }
    
    @Test
    public void Open_Connection() throws IOException {
        repository.initialize();
        RDFConnection conn = repository.openConnection();
        assertNotNull(conn);
        RDFBeanTransaction tx = conn.beginTransaction(false, 0, Connection.TRANSACTION_READ_COMMITTED);
        assertNotNull(tx);
        tx.commit();
        conn.close();
    }

    
}
