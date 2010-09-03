/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Test;

import com.mysema.rdfbean.domains.InferenceDomain.Entity1;
import com.mysema.rdfbean.domains.InferenceDomain.Entity2;
import com.mysema.rdfbean.domains.InferenceDomain.Entity3;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;

/**
 * RDBRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBRepositoryTest extends AbstractRDBTest{
    
    private RDBRepository repo;
    
    @After
    public void tearDown(){
        if (repo != null){
            repo.close();
        }
    }
    
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
    public void testOpenConnection() throws IOException {
        repository.initialize();
        RDFConnection conn = repository.openConnection();
        assertNotNull(conn);
        RDFBeanTransaction tx = conn.beginTransaction(false, 0, Connection.TRANSACTION_READ_COMMITTED);
        assertNotNull(tx);
        tx.commit();
        conn.close();
    }

    @Test
    public void withSources(){
        JdbcConnectionPool ds = JdbcConnectionPool.create("jdbc:h2:target/foaf", "sa", "");   
        ds.setMaxConnections(30);
        Configuration configuration = new DefaultConfiguration(Entity1.class, Entity2.class, Entity3.class, SimpleType.class, SimpleType2.class);
        RDFSource source = new RDFSource("classpath:/foaf.rdf", Format.RDFXML, "http://xmlns.com/foaf/0.1/");
        repo = new RDBRepository(configuration, ds, templates, new MemoryIdSequence(), source);
        repo.initialize();
        
        // export
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repo.export(Format.TURTLE, baos);
        System.out.println(new String(baos.toByteArray()));        
    }
}
