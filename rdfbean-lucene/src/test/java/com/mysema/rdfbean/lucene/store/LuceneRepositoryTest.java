/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.store;

import static com.mysema.rdfbean.lucene.RDFTestData.objects;
import static com.mysema.rdfbean.lucene.RDFTestData.predicates;
import static com.mysema.rdfbean.lucene.RDFTestData.subjects;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;

/**
 * LuceneRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneRepositoryTest extends AbstractStoreTest{
    
    private int count(CloseableIterator<STMT> stmts) throws IOException {
        int rv = 0;
        try{
            while (stmts.hasNext()){
                rv++;
                stmts.next();
            }    
        }finally{
            stmts.close();    
        }
        return rv;
    }

    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration();
    }

    @Test
    public void test() throws IOException{
        repository.initialize();
        RDFConnection connection = repository.openConnection();
        
        // tx #1
        RDFBeanTransaction tx = connection.beginTransaction(false, 0, 0);
        Set<STMT> added = new HashSet<STMT>();
        for (UID subject : subjects){
            for (UID predicate : predicates){
                for (NODE object : objects){
                    added.add(new STMT(subject, predicate, object));
                }
            }
        }
        connection.update(Collections.<STMT>emptySet(), added);
        tx.commit();
        
        // tx #2
        tx = connection.beginTransaction(true, 0, 0);
//        String msg = "wildcard failed";
//        assertEquals(subjects.size(), connection.countDocuments(null, null, null, null));        
        
        // documents counts
//        for (UID subject : subjects){
//            msg = "subject graph for "+ subject + " failed";
//            assertEquals(msg, 1, connection.countDocuments(subject, null, null, null));
//            
//            for (UID predicate : predicates){
//                msg = subject + " " + predicate + " ? failed";
//                assertEquals(msg, 1, connection.countDocuments(subject, predicate, null, null));
//                
//                for (NODE object : objects){
//                    msg = subject + " " + predicate + " " + object + " failed";
//                    assertEquals(msg, 1, connection.countDocuments(subject, predicate, object, null));
//                    msg = "? " + predicate + " " + object + " failed";
//                    assertEquals(msg, subjects.size(), connection.countDocuments(null, predicate, object, null));
//                    msg = "? ? " + object + " failed";
//                    assertEquals(msg, subjects.size(), connection.countDocuments(null, null, object, null));
//                }
//            }
//        }
        
        // statement counts
        
        for (UID subject : subjects){
            assertEquals(predicates.size() * objects.size(), count(connection.findStatements(subject, null, null, null, false)));
            
            for (UID predicate : predicates){
                assertEquals(objects.size(), count(connection.findStatements(subject, predicate, null, null, false)));
                
                for (NODE object : objects){
                    assertEquals(1, count(connection.findStatements(subject, predicate, object, null, false)));
                    assertEquals(subjects.size(), count(connection.findStatements(null, predicate, object, null, false)));                                        
                    assertEquals(subjects.size() * predicates.size(), count(connection.findStatements(null, null, object, null, false)));
                }
            }
        }
        
        
        tx.commit();
        connection.close();
    }
    


}
