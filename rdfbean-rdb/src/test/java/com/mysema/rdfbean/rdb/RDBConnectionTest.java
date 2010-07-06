package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;

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
    
    @Override
    public void setUp() throws SQLException{
        super.setUp();
        conn = repository.openConnection();
        jdbcConn = dataSource.getConnection();
    }
    
    @Override
    public void tearDown() throws IOException, SQLException{
        if (conn != null){
            conn.close();    
        }        
        if (jdbcConn != null){
            jdbcConn.close();
        }
        super.tearDown();
    }
    
    @Test
    public void testFindStatements() {
        Set<STMT> additions = new HashSet<STMT>();
        additions.add(new STMT(RDF.type, RDF.type, RDF.Property));
        additions.add(new STMT(RDF.type, RDFS.label, new LIT("type")));
        additions.add(new STMT(RDF.type, RDFS.label, new LIT("tyyppi", new Locale("fi"))));     
        Set<STMT> removals = new HashSet<STMT>();       
        conn.update(removals, additions);
        
        assertEquals(3, conn.find(RDF.type, null, null, null, false).size());
        assertEquals(1, conn.find(RDF.type, RDF.type, null, null, false).size());
        assertEquals(2, conn.find(RDF.type, RDFS.label, null, null, false).size());
        assertEquals(1, conn.find(RDF.type, RDFS.label, new LIT("type"), null, false).size());
    }

    @Test
    public void testUpdate() throws SQLException {
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
    public void testAddStatement() {
        // TODO
    }

    @Test
    public void testAddNode() {
        // TODO
    }

    @Test
    public void testRemoveStatement() {
        // TODO
    }

    private Long id(NODE node){
        return idFactory.getId(node);
    }

    private SQLQuery from(PEntity<?> entity){
        return new SQLQueryImpl(jdbcConn, templates).from(entity);
    }

}
