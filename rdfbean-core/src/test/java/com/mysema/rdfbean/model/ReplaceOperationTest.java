package com.mysema.rdfbean.model;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;

public class ReplaceOperationTest {
    
    private MiniRepository repository;

    @Before
    public void setUp(){
        repository = new MiniRepository();
        repository.add(new STMT(RDF.type,RDF.type,RDF.Property));
        repository.add(new STMT(RDF.Property,RDF.type,RDFS.Class));
    }
    
    @Test
    public void testExecute() throws IOException {
        UID replacement = new UID(RDF.NS,"type2");
        Operation replace = new ReplaceOperation(Collections.singletonMap(RDF.type, replacement));
        repository.execute(replace);
        List<STMT> stmts = IteratorAdapter.<STMT>asList(repository.findStatements(null, null, null, null, true));
        assertEquals(2, stmts.size());
        assertTrue(stmts.contains(new STMT(replacement,RDF.type,RDF.Property)));
        assertTrue(stmts.contains(new STMT(RDF.Property,RDF.type,RDFS.Class)));
    }
    
    @Test
    public void testExecute2() throws IOException {
        UID replacement = new UID(RDF.NS,"Property2");
        Operation replace = new ReplaceOperation(Collections.singletonMap(RDF.Property, replacement));
        repository.execute(replace);
        List<STMT> stmts = IteratorAdapter.<STMT>asList(repository.findStatements(null, null, null, null, true));
        assertEquals(2, stmts.size());
        assertTrue(stmts.contains(new STMT(RDF.type,RDF.type,replacement)));
        assertTrue(stmts.contains(new STMT(replacement,RDF.type,RDFS.Class)));
    }

}
