/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;

public class FetchStrategyTest {

    private BID subject = new BID();
    
    private MiniRepository repository;
    
    @Before
    public void setUp(){
        repository = new MiniRepository();
        repository.add(
                new STMT(subject, RDFS.label,   new LIT("test1"), RDFS.label),
                new STMT(subject, RDFS.comment, new LIT("test2"), RDFS.label));
    }
    
    @Test
    public void testPredicateWildcardFetch() throws IOException {
        FetchOptimizer fetchOptimizer = new FetchOptimizer(repository.openConnection(), new PredicateWildcardFetch());
        CloseableIterator<STMT> stmts = fetchOptimizer.findStatements(subject, null, null, null, false);
        assertTrue(stmts.hasNext());
        stmts.close();
        
        // fetch all
        stmts = fetchOptimizer.findStatements(null, null, null, null, true);
        assertTrue(stmts.hasNext());
        stmts.close();
    }
    
    @Test
    public void testFullContextFetch() throws IOException {
        FullContextFetch fullContextFetch = new FullContextFetch();
        fullContextFetch.setContexts(Collections.singleton(RDFS.label));
        FetchOptimizer fetchOptimizer = new FetchOptimizer(repository.openConnection(), fullContextFetch);
        CloseableIterator<STMT> stmts = fetchOptimizer.findStatements(null, null, null, RDFS.label, false);
        assertTrue(stmts.hasNext());
        stmts.close();

        // fetch all
        stmts = fetchOptimizer.findStatements(null, null, null, null, true);
        assertTrue(stmts.hasNext());
        stmts.close();
    }
    
    @Test
    public void testFullContextFetch_no_contexts_given() throws IOException {
        FullContextFetch fullContextFetch = new FullContextFetch();
//        fullContextFetch.setContexts(Collections.singleton(RDFS.label));
        FetchOptimizer fetchOptimizer = new FetchOptimizer(repository.openConnection(), fullContextFetch);
        CloseableIterator<STMT> stmts = fetchOptimizer.findStatements(null, null, null, RDFS.label, false);
        assertTrue(stmts.hasNext());
        stmts.close();

        // fetch all
        stmts = fetchOptimizer.findStatements(null, null, null, null, true);
        assertTrue(stmts.hasNext());
        stmts.close();
    }
    
}
