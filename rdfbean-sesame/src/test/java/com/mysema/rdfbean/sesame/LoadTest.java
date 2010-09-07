package com.mysema.rdfbean.sesame;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;

public class LoadTest {
    
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

}
