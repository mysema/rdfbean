package com.mysema.rdfbean.jena;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;


public abstract class AbstractConnectionTest {
    
    protected MemoryRepository repository;

    protected RDFConnection connection;

    @Before
    public void setUp(){
        repository = new MemoryRepository();
        repository.addGraph(new UID(TEST.NS));
        repository.initialize();
        connection = repository.openConnection();
    }

    @After
    public void tearDown(){
        connection.close();
        repository.close();
    }
    
    protected void assertExists(STMT stmt){
        assertExists(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext()); // s p o
        assertExists(stmt.getSubject(), stmt.getPredicate(), null,             stmt.getContext()); // s p -
        assertExists(stmt.getSubject(), null,                null,             stmt.getContext()); // s - - 
        assertExists(null,              stmt.getPredicate(), stmt.getObject(), stmt.getContext()); // - p o
        assertExists(null,              null,                stmt.getObject(), stmt.getContext()); // - - o       
    }
        
    protected List<STMT> findStatements(ID subject, UID predicate, NODE object, UID context){
        return IteratorAdapter.asList(connection.findStatements(subject, predicate, object, context, false));
    }
    
    protected void assertExists(ID subject, UID predicate, NODE object, UID context){
        assertTrue(connection.exists(subject, predicate, object, context, false));
    }
   
    protected void assertNotExists(ID subject, UID predicate, NODE object, UID context){
        assertFalse(connection.exists(subject, predicate, object, context, false));
    }

}
