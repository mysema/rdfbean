/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;

/**
 * BlankNodeTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BlankNodeTest {
    
    private Repository repository;
    
    @After
    public void tearDown(){
        repository.close();
    }
    
    @Test
    public void WithMiniRepository() throws Exception{
        test(new MiniRepository());
    }
    
    @Test
    public void WithNativeRepository() throws Exception{
        File dataDir = new File("target/test-repo1");
        FileUtils.deleteDirectory(dataDir);
        NativeRepository repository = new NativeRepository();
        repository.setDataDir(dataDir);
        test(repository);
    }
    
    @Test
    @Ignore
    public void WithMemoryRepository() throws Exception{
        File dataDir = new File("target/test-repo2");
        FileUtils.deleteDirectory(dataDir);
        MemoryRepository repository = new MemoryRepository();
        repository.setDataDir(dataDir);
        test(repository);
    }
    
    protected void test(Repository repository) throws IOException{
        this.repository = repository;
        repository.initialize();
        
        STMT stmt = new STMT(new BID(), CORE.localId, new LIT("test"));
        RDFConnection conn = repository.openConnection();
        try{
            conn.update(Collections.<STMT>emptySet(), Collections.singleton(stmt));
        }finally{
            conn.close();
        }
        
        conn = repository.openConnection();
        try{
            CloseableIterator<STMT> stmts = conn.findStatements(null, CORE.localId, null, null, false);
            try{
                assertTrue(stmts.hasNext());
                STMT other = stmts.next();
                assertEquals(stmt.getSubject(), other.getSubject());
            }finally{
                stmts.close();
            }
        }finally{
            conn.close();
        }
    }

}
