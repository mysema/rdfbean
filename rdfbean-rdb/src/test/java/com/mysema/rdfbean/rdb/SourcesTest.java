/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Test;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.rdfbean.model.IdSequence;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;

public class SourcesTest {
    
    private Configuration configuration = new DefaultConfiguration();
    
    private SQLTemplates templates = new H2Templates();
    
    private RDBRepository repo;
    
    @After
    public void tearDown(){
        if (repo != null){
            repo.close();
        }
    }    
    
    @Test
    public void WithSources(){
        JdbcConnectionPool ds = JdbcConnectionPool.create("jdbc:h2:target/foaf", "sa", "");   
        ds.setMaxConnections(30);
        RDFSource source = new RDFSource("classpath:/foaf.rdf", Format.RDFXML, "http://xmlns.com/foaf/0.1/");
        repo = new RDBRepository(configuration, ds, templates, new MemoryIdSequence(), source);
        repo.initialize();
        
        // export
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repo.export(Format.TURTLE, null, baos);
        assertTrue(baos.toByteArray().length > 0);
    }
    
    @Test
    public void RdfXmlSource() throws IOException{
        IdSequence sequence = new MemoryIdSequence();
        JdbcConnectionPool ds = JdbcConnectionPool.create("jdbc:h2:target/mixed1", "sa", "");   
        ds.setMaxConnections(30);
        RDFSource source1= new RDFSource("classpath:/foaf.rdf", Format.RDFXML, "http://xmlns.com/foaf/0.1/");
        RDFSource source2 = new RDFSource("classpath:/wine.owl", Format.RDFXML, "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#");
        
        // 1st count
        repo = new RDBRepository(configuration, ds, templates, sequence, source1, source2);
        repo.initialize();
        RDBConnection conn = repo.openConnection();
        int count = conn.find(null, null, null, null, false).size();
        conn.close();
        repo.close();
        
        // 2nd count
        repo = new RDBRepository(configuration, ds, templates, sequence, source1, source2);
        repo.initialize();
        conn = repo.openConnection();
        try{
            assertEquals(count, conn.find(null, null, null, null, false).size());    
        }finally{
            conn.close();
            repo.close();    
            repo = null;
        }
    }
    
    @Test
    public void TurtleSource() throws IOException{
        IdSequence sequence = new MemoryIdSequence();
        JdbcConnectionPool ds = JdbcConnectionPool.create("jdbc:h2:target/mixed2", "sa", "");   
        ds.setMaxConnections(30);        
        RDFSource source = new RDFSource("classpath:/test.ttl", Format.TURTLE, "http://semantics.mysema.com/test#");
        
        // 1st count
        repo = new RDBRepository(configuration, ds, templates, sequence, source);
        repo.initialize();
        RDBConnection conn = repo.openConnection();
        int count = conn.find(null, null, null, null, false).size();
        conn.close();
        repo.close();
        
        // 2nd count
        repo = new RDBRepository(configuration, ds, templates, sequence, source);
        repo.initialize();
        conn = repo.openConnection();
        try{
            assertEquals(count, conn.find(null, null, null, null, false).size());    
        }finally{
            conn.close();
            repo.close();    
            repo = null;
        }
    }

}
