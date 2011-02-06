/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.object.Session;


/**
 * MultiConnectionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MultiConnectionTest {

    private MiniDialect dialect = new MiniDialect();
    
    private MiniRepository repository = new MiniRepository();
    
    @Test
    public void test() throws IOException{        
        MultiConnection connection = new MultiConnection(
            repository.openConnection(),
            repository.openConnection()
        ){
            @Override
            public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
                throw new UnsupportedOperationException();
            }

            @Override
            public QueryOptions getQueryOptions() {
                return QueryOptions.DEFAULT;
            }
            
        };
            
        // find
        CloseableIterator<STMT> stmts = connection.findStatements(null, null, null, null, false);
        assertFalse(stmts.hasNext());
        stmts.close();
        
        // update
        connection.update(
                Collections.<STMT>emptySet(), 
                Collections.<STMT>singleton(dialect.createStatement(RDFS.label, RDF.type, RDF.Property)));
        
        // find again
        stmts = connection.findStatements(RDFS.label, RDF.type, null, null, false);
        assertTrue(stmts.hasNext());
        stmts.close();
        
        // close
        connection.close();        
        
    }
    
}
