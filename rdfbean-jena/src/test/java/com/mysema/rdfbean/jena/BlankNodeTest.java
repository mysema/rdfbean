/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;

public class BlankNodeTest {
    
    private Repository repository;

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
    public void test() throws IOException{        
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
