package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public abstract class AbstractConnectionTest {

    protected static VirtuosoRepository repository;
    
    protected static UID context = new UID(TEST.NS, "named1");
    
    protected static UID context2 = new UID(TEST.NS, "named2");
    
    protected VirtuosoRepositoryConnection connection;
    
    protected Collection<STMT> toBeRemoved;
    
    @BeforeClass
    public static void setUpClass(){
        repository = new VirtuosoRepository("localhost:1111", "dba", "dba", TEST.NS);
        repository.setAllowedGraphs(Arrays.asList(context, context2));
        repository.initialize();
    }

    @AfterClass
    public static void tearDownClass(){
        repository.close();
    }

    @Before
    public void setUp(){
        connection = repository.openConnection();
    }

    @After
    public void tearDown(){
        if (connection != null){
            if (toBeRemoved != null){
                connection.update(toBeRemoved, null);
            }            
            connection.close();
        }
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
