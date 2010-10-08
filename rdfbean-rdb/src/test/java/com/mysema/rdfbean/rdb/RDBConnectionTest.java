/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.UtilDateConverter;

/**
 * RDBConnectionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBConnectionTest extends AbstractRDBTest{

    private IdFactory idFactory = new MD5IdFactory();
    
    private RDBConnection conn;
    
    private Connection jdbcConn;
    
    @Before
    public void setUp() throws SQLException{
        conn = repository.openConnection();
        jdbcConn = dataSource.getConnection();
    }
    
    @After
    public void tearDown() throws IOException, SQLException{
        if (conn != null){
            conn.close();    
        }        
        if (jdbcConn != null){
            jdbcConn.close();
        }
    }
    
    @Test
    public void FindStatements() {
        ID subject = new BID();
        Set<STMT> additions = new HashSet<STMT>();
        additions.add(new STMT(subject, RDF.type, RDF.Property));
        additions.add(new STMT(subject, RDFS.label, new LIT("type")));
        additions.add(new STMT(subject, RDFS.label, new LIT("tyyppi", new Locale("fi"))));     
        Set<STMT> removals = new HashSet<STMT>();       
        conn.update(removals, additions);
        
        assertEquals(3, conn.find(subject, null, null, null, false).size());
        assertEquals(1, conn.find(subject, RDF.type, null, null, false).size());
        assertEquals(2, conn.find(subject, RDFS.label, null, null, false).size());
        assertEquals(1, conn.find(subject, RDFS.label, new LIT("type"), null, false).size());
    }
    
    @Test
    public void DateTime(){
        UtilDateConverter converter = new UtilDateConverter();
        ID subject = new BID();
        LIT object = new LIT(converter.toString(new Date()), XSD.date);
        Set<STMT> additions = new HashSet<STMT>();
        additions.add(new STMT(subject, new UID(TEST.NS,"created"),object));
        Set<STMT> removals = new HashSet<STMT>();       
        conn.update(removals, additions);
        
        assertEquals(1, conn.find(subject, null, null, null, false).size());
        assertEquals(1, conn.find(subject, new UID(TEST.NS,"created"), null, null, false).size());
        assertEquals(1, conn.find(subject, new UID(TEST.NS,"created"), object, null, false).size());        
    }
    
    @Test
    public void Numeric(){
        ID subject = new BID();
        Set<STMT> additions = new HashSet<STMT>();
        additions.add(new STMT(subject, new UID(TEST.NS,"int"), new LIT("1",XSD.intType)));
        additions.add(new STMT(subject, new UID(TEST.NS,"double"), new LIT("1.0",XSD.doubleType)));
        Set<STMT> removals = new HashSet<STMT>();       
        conn.update(removals, additions);
        
        assertEquals(2, conn.find(subject, null, null, null, false).size());
        assertEquals(1, conn.find(subject, new UID(TEST.NS,"int"), null, null, false).size());
        assertEquals(1, conn.find(subject, new UID(TEST.NS,"int"), new LIT("1",XSD.intType), null, false).size());
        assertEquals(1, conn.find(subject, new UID(TEST.NS,"double"),  new LIT("1.0",XSD.doubleType), null, false).size());
    }

    @Test
    public void Update() throws SQLException {
       Set<STMT> additions = new HashSet<STMT>();
       additions.add(new STMT(RDF.type, RDF.type, RDF.Property));
       additions.add(new STMT(RDF.type, RDFS.label, new LIT("type")));
       additions.add(new STMT(RDF.type, RDFS.label, new LIT("tyyppi", new Locale("fi"))));     
       Set<STMT> removals = new HashSet<STMT>();       
       conn.update(removals, additions);
       
       // print inserted triples
       QStatement stmt = QStatement.statement;
       QSymbol sub = new QSymbol("sub");
       QSymbol pre = new QSymbol("pre");
       QSymbol obj = new QSymbol("obj");
       SQLQuery query = from(stmt);
       query.where(stmt.subject.eq(id(RDF.type)));
       query.innerJoin(stmt.subjectFk, sub);
       query.innerJoin(stmt.predicateFk, pre);
       query.innerJoin(stmt.objectFk, obj);
       for (Object[] row : query.list(sub.lexical, pre.lexical, obj.lexical)){
           System.out.println(Arrays.asList(row));
       }
       
       QSymbol symbol = QSymbol.symbol;
       assertEquals(1l, from(symbol).where(symbol.id.eq(id(RDF.type))).count());
       assertEquals(1l, from(symbol).where(symbol.id.eq(id(RDFS.label))).count());
       assertEquals(1l, from(symbol).where(symbol.id.eq(id(new LIT("type")))).count());
       
       assertEquals((long)additions.size(), from(stmt).where(stmt.subject.eq(id(RDF.type))).count());       
    }
    

    @Test
    public void AddStatement() {
        Set<STMT> additions = new HashSet<STMT>();
        ID sub1 = new BID();
        ID sub2 = new BID();
        additions.add(new STMT(sub1, RDF.type, RDF.Property));        
        additions.add(new STMT(sub1, RDFS.label, new LIT("type")));
        additions.add(new STMT(sub1, RDFS.label, new LIT("tyyppi", new Locale("fi"))));
        additions.add(new STMT(sub2, RDF.type, RDF.Property, new UID(RDF.NS)));
        conn.update(Collections.<STMT>emptySet(), additions);        
        
        for (STMT stmt : additions){
            assertEquals(stmt + " failed", 1, conn.find(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext(), false).size());
        }
    }

    @Test
    public void AddNode() {
        // TODO
    }

    @Test
    public void RemoveStatement() {
        Set<STMT> removals = new HashSet<STMT>();
        ID sub1 = new BID();
        ID sub2 = new BID();
        removals.add(new STMT(sub1, RDF.type, RDF.Property));        
        removals.add(new STMT(sub1, RDFS.label, new LIT("type")));
        removals.add(new STMT(sub1, RDFS.label, new LIT("type"), new UID(RDF.NS)));
        removals.add(new STMT(sub1, RDFS.label, new LIT("tyyppi", new Locale("fi"))));
        removals.add(new STMT(sub2, RDF.type, RDF.Property));
        removals.add(new STMT(sub2, RDF.type, RDF.Property, new UID(RDF.NS)));
        conn.update(removals, Collections.<STMT>emptySet());        
        
        for (STMT stmt : removals){
            assertEquals(stmt + " failed", 0, conn.find(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext(), false).size());
        }
    }
    
    private Long id(NODE node){
        return idFactory.getId(node);
    }

    private SQLQuery from(RelationalPath<?> entity){
        return new SQLQueryImpl(jdbcConn, templates).from(entity);
    }

}
