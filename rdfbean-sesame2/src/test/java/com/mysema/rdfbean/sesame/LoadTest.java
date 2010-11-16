package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Addition;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.CountOperation;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;

public class LoadTest {
    
    private Operation<Long> countOp = new CountOperation(); 
    
    private MemoryRepository repository;
    
    @Before
    public void setUp(){
        repository = new MemoryRepository();
        repository.initialize();
    }
    
    @After
    public void tearDown(){
        repository.close();
    }
    
    @Test
    public void Export_and_Load(){
        InputStream is = getClass().getResourceAsStream("/test.ttl");
        UID context = new UID(TEST.NS);
        repository.load(Format.TURTLE, is, context, true);
        long count1 = repository.execute(countOp);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, baos);
        
        MemoryRepository repository2 = new MemoryRepository();
        repository2.initialize();
        repository2.load(Format.TURTLE, new ByteArrayInputStream(baos.toByteArray()), new UID(TEST.NS), true);
        long count2 = repository.execute(countOp);
        repository2.close();
        assertEquals(count1, count2);
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
    
}
