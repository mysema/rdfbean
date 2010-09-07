/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;

/**
 * RDBRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBRepositoryTest extends AbstractRDBTest{
    
    @Test
    public void testExecute() {
        // TODO
    }

    @Test
    public void testExport() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, baos);
        System.out.println(new String(baos.toByteArray()));
    }

    @Test
    public void testLoad_withContext_replace(){
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        UID context = new UID(TEST.NS);
        repository.load(Format.TURTLE, is, context, true);
    }
    
    @Test
    public void testLoad_withContext_noReplace(){
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        UID context = new UID(TEST.NS);
        repository.load(Format.TURTLE, is, context, false);
    }
    
    @Test
    @Ignore
    public void testLoad_withoutContext(){
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        repository.load(Format.TURTLE, is, null, false);
    }
    
    @Test
    public void testOpenConnection() throws IOException {
        repository.initialize();
        RDFConnection conn = repository.openConnection();
        assertNotNull(conn);
        RDFBeanTransaction tx = conn.beginTransaction(false, 0, Connection.TRANSACTION_READ_COMMITTED);
        assertNotNull(tx);
        tx.commit();
        conn.close();
    }

    
}
