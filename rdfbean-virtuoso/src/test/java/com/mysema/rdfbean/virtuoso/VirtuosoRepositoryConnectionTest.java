package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Addition;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

public class VirtuosoRepositoryConnectionTest extends AbstractConnectionTest{

    @Test
    public void Exists() {
        ID sub = new UID(TEST.NS, "e"+ System.currentTimeMillis());
        ID type = new UID(TEST.NS, "TestType" + System.currentTimeMillis());
        assertFalse(connection.exists(sub, null, null, null, false));
        repository.execute(new Addition(new STMT(sub, RDF.type, type)));
        toBeRemoved = Collections.singleton(new STMT(sub, RDF.type, type));
        assertTrue(connection.exists(null, RDF.type, type, null, false));
        assertTrue(connection.exists(null, RDF.type, null, null, false));
        assertTrue(connection.exists(sub,  null,     null, null, false));
    }

    @Test
    public void FindStatements() {
        ID sub = new UID(TEST.NS, UUID.randomUUID().toString());
        assertTrue(IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false)).isEmpty());
        repository.execute(new Addition(new STMT(sub, RDF.type, RDFS.Class)));
        toBeRemoved = Collections.singleton(new STMT(sub, RDF.type, RDFS.Class));
        
        assertFalse(IteratorAdapter.asList(connection.findStatements(sub,  null,     null,       null, false)).isEmpty());
        assertFalse(IteratorAdapter.asList(connection.findStatements(sub,  RDF.type, null,       null, false)).isEmpty());
        assertFalse(IteratorAdapter.asList(connection.findStatements(null, RDF.type, RDFS.Class, null, false)).isEmpty());
    }
    
    @Test
    public void FindStatements_from_Context() {
        UID sub = new UID(TEST.NS, UUID.randomUUID().toString());
        assertTrue(IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false)).isEmpty());
        repository.execute(new Addition(new STMT(sub, RDF.type, RDFS.Class, sub)));
        toBeRemoved = Collections.singleton(new STMT(sub, RDF.type, RDFS.Class, sub));
        
        assertFalse(IteratorAdapter.asList(connection.findStatements(sub,  null, null, null, false)).isEmpty());
        assertFalse(IteratorAdapter.asList(connection.findStatements(sub,  null, null, sub, false)).isEmpty());
        assertFalse(IteratorAdapter.asList(connection.findStatements(null, null, null, sub, false)).isEmpty());
    }
    
    @Test
    public void Resources(){
        ID sub = new UID(TEST.NS, "e"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(new STMT(sub, RDFS.label, sub));
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        List<STMT> found = IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false));
        assertEquals(new HashSet<STMT>(stmts), new HashSet<STMT>(found));
        
        assertTrue(connection.exists(sub,  null,       null, null, false));
        assertTrue(connection.exists(sub,  RDFS.label, null, null, false));
        assertTrue(connection.exists(sub,  RDFS.label, sub,  null, false));
        assertTrue(connection.exists(null, RDFS.label, sub,  null, false));
        assertTrue(connection.exists(null,             null, sub, null, false));
    }
    
    @Test
    public void Literals(){
        ID sub = new UID(TEST.NS, "e"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, RDFS.label, new LIT(sub.getId())),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), Locale.ENGLISH)),
                new STMT(sub, RDFS.label, new LIT("1", XSD.intType))
                );
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        List<STMT> found = IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false));
        assertEquals(new HashSet<STMT>(stmts), new HashSet<STMT>(found));
        
        // find int literal
        assertTrue(connection.exists(sub,  null,       null,                      null, false));
        assertTrue(connection.exists(sub,  RDFS.label, null,                      null, false));
        assertTrue(connection.exists(sub,  RDFS.label, new LIT("1", XSD.intType), null, false));
        assertTrue(connection.exists(null, RDFS.label, new LIT("1", XSD.intType), null, false));
        assertTrue(connection.exists(null, null,       new LIT("1", XSD.intType), null, false));
        
        // find string literal
        assertTrue(connection.exists(sub,  null,       null,                 null, false));
        assertTrue(connection.exists(sub,  RDFS.label, null,                 null, false));
        assertTrue(connection.exists(sub,  RDFS.label, new LIT(sub.getId()), null, false));
        assertTrue(connection.exists(null, RDFS.label, new LIT(sub.getId()), null, false));
        assertTrue(connection.exists(null, null,       new LIT(sub.getId()), null, false));
    }

    @Test
    public void BlankNodes(){
        ID sub = new BID();
        ID obj = new BID();
        List<STMT> stmts = Collections.singletonList(new STMT(sub, RDF.type, obj));
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        List<STMT> found = IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false));
        assertEquals(new HashSet<STMT>(stmts), new HashSet<STMT>(found));
        
        assertTrue(connection.exists(sub,  null,     null, null, false));
        assertTrue(connection.exists(sub,  RDF.type, null, null, false));
        assertTrue(connection.exists(sub,  RDF.type, obj,  null, false));
        assertTrue(connection.exists(null, RDF.type, obj,  null, false));
        assertTrue(connection.exists(null, null,     obj,  null, false));
    }
    
    @Test
    public void Remove_none_given_from_default(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        assertTrue(connection.exists(stmt.getSubject(), null, null, null, false));
        connection.remove(null, null, null, null);
        assertFalse(connection.exists(stmt.getSubject(), null, null, null, false));
    }
    
    @Test
    public void Remove_none_given_from_named(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj,sub.asURI());
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        assertTrue(connection.exists(stmt.getSubject(), null, null, null, false));
        connection.remove(null, null, null, null);
        assertFalse(connection.exists(stmt.getSubject(), null, null, null, false));
    }
    
    @Test
    public void Remove_all_given_from_default(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        assertTrue(connection.exists(stmt.getSubject(), null, null, null, false));
        connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
        assertFalse(connection.exists(stmt.getSubject(), null, null, null, false));
    }
    
    @Test
    public void Remove_all_given_from_named(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj, sub.asURI());
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        assertTrue(connection.exists(stmt.getSubject(), null, null, null, false));
        connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
        assertFalse(connection.exists(stmt.getSubject(), null, null, null, false));
    }
    
    @Test
    public void Remove_subject_given(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        assertTrue(connection.exists(stmt.getSubject(), null, null, null, false));
        connection.remove(stmt.getSubject(), null, null, null);
        assertFalse(connection.exists(stmt.getSubject(), null, null, null, false));
    }
    
    @Test
    public void Remove_Literals_from_named(){
        ID sub = new UID(TEST.NS, "el"+ System.currentTimeMillis());
        UID context = new UID("sesame:other");
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, RDFS.label, new LIT(sub.getId(), new UID(TEST.NS)), context),
                new STMT(sub, RDFS.label, new LIT(sub.getId()), context),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), Locale.ENGLISH), context),
                new STMT(sub, RDFS.label, new LIT("1", XSD.intType), context),
                new STMT(sub, RDFS.label, new LIT("20011010", XSD.date), context)
                );
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        for (STMT stmt : stmts){
            connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
            assertFalse(stmt.toString(), connection.exists(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext(), false));
        }
        assertFalse(connection.exists(sub, null, null, null, false));
    }

    @Test
    public void Remove_Literals_from_default(){
        ID sub = new UID(TEST.NS, "el"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, new UID(TEST.NS), new LIT("x", new UID(TEST.NS))),
                new STMT(sub, new UID(TEST.NS), new LIT("1", new UID(TEST.NS))),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), new UID(TEST.NS))),
                new STMT(sub, RDFS.label, new LIT(sub.getId())),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), Locale.ENGLISH)),
                new STMT(sub, RDFS.label, new LIT("1", XSD.intType)),
                new STMT(sub, new UID(TEST.NS), new LIT("1", XSD.intType)),
                new STMT(sub, RDFS.label, new LIT("20011010", XSD.date)),
                new STMT(sub, new UID(TEST.NS), new LIT("20011010", XSD.date))
                );
        toBeRemoved = stmts;
        connection.update(null, stmts);
        
        for (STMT stmt : stmts){
            connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
            assertFalse(stmt.toString(), connection.exists(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext(), false));
        }
        for(STMT stmt : IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false))){
            System.err.println(stmt);
        }
        assertFalse(connection.exists(sub, null, null, null, false));
    }
    
    @Test
    public void Update() {
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID type = new UID(TEST.NS, "TestType" + System.currentTimeMillis());
        Collection<STMT> stmts = Collections.singleton(new STMT(sub, RDF.type, type));
        toBeRemoved = stmts;        
        // add
        connection.update(null, stmts);
        assertTrue(connection.exists(sub, RDF.type, type, null, false));        
        // remove
        connection.update(stmts, null);
        assertFalse(connection.exists(sub, RDF.type, type, null, false));
    }
    
    @Test
    public void Update_named() {
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID type = new UID(TEST.NS, "TestType" + System.currentTimeMillis());
        Collection<STMT> stmts = Collections.singleton(new STMT(sub, RDF.type, type, sub.asURI()));
        toBeRemoved = stmts;        
        // add
        connection.update(null, stmts);
        assertTrue(connection.exists(sub, RDF.type, type, null, false));        
        // remove
        connection.update(stmts, null);
        assertFalse(connection.exists(sub, RDF.type, type, null, false));
    }

}
