package com.mysema.rdfbean.model;

import static com.mysema.rdfbean.model.QNODE.o;
import static com.mysema.rdfbean.model.QNODE.p;
import static com.mysema.rdfbean.model.QNODE.s;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RDFUpdateTest {
    
    private static final UID example = new UID("http://example.com");
    
    private static final UID ex1 = new UID("http://ex1.com");
    
    private static final UID ex2 = new UID("http://ex2.com");

    private Repository repository;
    
    private RDFConnection connection;
        
    @Before
    public void before(){
        repository = new MiniRepository();
        repository.initialize();
        connection = repository.openConnection();
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class)));
    }
    
    
    @After
    public void after(){
        if (connection != null) {
            connection.close();
        }
        repository.close();
    }
    
    @Test
    public void Delete_Where_No_Match() throws IOException{
//        SPARQLUpdate delete = parse("DELETE { ?s rdf:type <http://example.com> } WHERE { ?s ?p ?o }");
        RDFUpdate delete = update().delete(s.a(example)).where(s.has(p, o));
        delete.execute();
        
        assertTrue(connection.exists(null, null, null, null, false));                
    }
    
    @Test
    @Ignore
    public void Delete_Where_Matches() throws IOException{
//        parse("DELETE { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }").execute();
        RDFUpdate delete = update().delete(s.a(RDFS.Class)).where(s.has(p, o));
        delete.execute();
        
        assertFalse(connection.exists(null, null, null, null, false));
    }
    
    @Test
    @Ignore
    public void Delete_From_Where() throws IOException{
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        
//        SPARQLUpdate delete = parse("DELETE FROM <http://ex1.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        RDFUpdate delete = update().delete(s.a(RDFS.Class)).from(ex1).where(s.has(p,o));
        delete.execute();
        
        assertFalse(connection.exists(null, null, null, ex1, false));
        assertTrue(connection.exists(null, null, null, null, false));    
    }
    
    @Test
    @Ignore
    public void Delete_From_From_Where() throws IOException{
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex2)));
        
//        SPARQLUpdate delete = parse("DELETE FROM <http://ex1.com> FROM <http://ex2.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        RDFUpdate delete = update().delete(s.a(RDFS.Class)).from(ex1).where(s.has(p,o));
        delete.execute();
        
        assertFalse(connection.exists(null, null, null, ex1, false));
        assertFalse(connection.exists(null, null, null, ex2, false));
        assertTrue(connection.exists(null, null, null, null, false));  
    }
    
    @Test
    public void Insert_Where() throws IOException{
//        SPARQLUpdate insert = parse("INSERT { ?s rdf:type <http://ex2.com> } WHERE { ?s ?p ?o }");
        RDFUpdate insert = update().insert(s.a(ex2)).where(s.has(p,o));
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, ex2, null, false));
    }
    
    @Test
    public void Insert_Into_Where() throws IOException{
//        SPARQLUpdate insert = parse("INSERT INTO <http://ex1.com> { ?s rdf:type <http://ex2.com> } WHERE { ?s ?p ?o }");
        RDFUpdate insert = update().insert(s.a(ex2)).into(ex1).where(s.has(p,o));
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, ex2, ex1, false));
    }
    
    @Test
    public void Insert_Into_Into_Where() throws IOException{
//        SPARQLUpdate insert = parse("INSERT INTO <http://ex1.com> INTO <http://ex2.com> { ?s rdf:type <http://example.com> } WHERE { ?s ?p ?o }");
        RDFUpdate insert = update().insert(s.a(example)).into(ex1, ex2).where(s.has(p,o));
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, example, ex1, false));
        assertTrue(connection.exists(RDFS.Resource, RDF.type, example, ex2, false));
    }
            
    @Test
    public void Modify() throws IOException{
//        SPARQLUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
//        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_Empty_Delete() throws IOException{
//        SPARQLUpdate modify = parse("MODIFY DELETE {} INSERT { ?s ?p2 ?o2 }");
//        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_Empty_Insert() throws IOException{
//        SPARQLUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT {}");
//        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_URI() throws IOException{
//        SPARQLUpdate modify = parse("MODIFY <http://ex1.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
//        modify.execute();
        
        // TODO : assertions
    }  
    
    @Test
    public void Modify_URI_URI() throws IOException{
//        SPARQLUpdate modify = parse("MODIFY <http://ex1.com> <http://ex2.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
//        modify.execute();
        
        // TODO : assertions
    }  

    @Test
    public void Modify_Where() throws IOException{
//        SPARQLUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s2 ?p2 ?o2 } WHERE { ?s3 ?p3 ?o3 }");
//        modify.execute();
        
        // TODO : assertions
    }
    
    private RDFUpdate update(){
        return new RDFUpdateImpl(connection);
    }

}
