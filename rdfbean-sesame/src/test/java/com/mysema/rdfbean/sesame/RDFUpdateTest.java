package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.DC;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.RDFUpdate;
import com.mysema.rdfbean.model.RDFUpdateImpl;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class RDFUpdateTest {
    
    private static final UID example = new UID("http://example.com");
    
    private static final UID ex1 = new UID("http://ex1.com");
    
    private static final UID ex2 = new UID("http://ex2.com");

    private static final String PREFIXES = "PREFIX rdf: <"+RDF.NS+">\nPREFIX rdfs: <"+RDFS.NS+">\n";
    
    private MemoryRepository repository;
    
    private RDFConnection connection;
        
    @Before
    public void before(){
        repository = new MemoryRepository();
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
    public void Prefixes() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("PREFIX dc: <http://purl.org/dc/elements/1.1/>\n");
        builder.append("INSERT { <http://example/egbook3> dc:title  \"This is an example title\" }");
        RDFUpdate insert = parse(builder.toString());
        insert.execute();
        
        assertTrue(connection.exists(new UID("http://example/egbook3"), DC.title, null, null, false));
    }
    
    @Test
    @Ignore // FIXME
    public void Clear() throws IOException{
        RDFUpdate clear = parse("CLEAR");
        clear.execute();
        
        assertFalse(connection.exists(null, null, null, null, false));
    }
    
    @Test
    public void Clear_Graph() throws IOException{
        connection.update(null, Arrays.asList(
                new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1),
                new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex2)));
        
        RDFUpdate clear = parse("CLEAR GRAPH <http://ex1.com>");
        clear.execute();
        
        assertFalse(connection.exists(null, null, null, ex1, false));
        assertTrue(connection.exists(null, null, null, ex2, false));
        
    }

    @Test
    public void Create() throws IOException{
        RDFUpdate create = parse("CREATE GRAPH <http://example.com>");
        create.execute();
        
        // no effect
    }
    
    @Test
    public void Create_Silent() throws IOException {
        RDFUpdate create = parse("CREATE SILENT GRAPH <http://example.com>");
        create.execute();
        
        // no effect
    }
    
    @Test
    public void Delete_Data() throws IOException{
        RDFUpdate delete = parse("DELETE DATA { rdfs:Resource rdf:type rdfs:Class }");
        delete.execute();
        
        assertFalse(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, null, false));
    }
    
    @Test
    public void Delete_Data_From() throws IOException {
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        
        RDFUpdate delete = parse("DELETE DATA FROM <http://ex1.com> { rdfs:Resource rdf:type rdfs:Class }");
        delete.execute();
        
        assertFalse(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, ex1, false));
        assertTrue(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, null, false));
    }
    
    @Test
    public void Delete_Data_From_From() throws IOException {
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex2)));
        
        RDFUpdate delete = parse("DELETE DATA FROM <http://ex1.com> FROM <http://ex2.com> { rdfs:Resource rdf:type rdfs:Class }");
        delete.execute();
        
        assertFalse(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, ex1, false));
        assertFalse(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, ex2, false));
        assertTrue(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, null, false));
    }
    
    @Test
    public void Delete_Where_No_Match() throws IOException{
        RDFUpdate delete = parse("DELETE { ?s rdf:type <http://example.com> } WHERE { ?s ?p ?o }");
        delete.execute();
        
        assertTrue(connection.exists(null, null, null, null, false));                
    }
    
    @Test
    public void Delete_Where_Matches() throws IOException{
        parse("DELETE { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }").execute();
        
        assertFalse(connection.exists(null, null, null, null, false));
    }
    
    @Test
    public void Delete_From_Where() throws IOException{
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        
        RDFUpdate delete = parse("DELETE FROM <http://ex1.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        delete.execute();
        
        assertFalse(connection.exists(null, null, null, ex1, false));
        assertTrue(connection.exists(null, null, null, null, false));    
    }
    
    @Test
    public void Delete_From_From_Where() throws IOException{
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex2)));
        
        RDFUpdate delete = parse("DELETE FROM <http://ex1.com> FROM <http://ex2.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        delete.execute();
        
        assertFalse(connection.exists(null, null, null, ex1, false));
        assertFalse(connection.exists(null, null, null, ex2, false));
        assertTrue(connection.exists(null, null, null, null, false));  
    }
    
    @Test
    public void Drop() throws IOException{
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, example)));
        
        RDFUpdate create = parse("DROP GRAPH <http://example.com>");
        create.execute();
        
        assertFalse(connection.exists(null, null, null, example, false));
        assertTrue(connection.exists(null, null, null, null, false));
    }
    
    @Test
    public void Drop_Silent() throws IOException {
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, example)));
        
        RDFUpdate create = parse("DROP SILENT GRAPH <http://example.com>");
        create.execute();
        
        assertFalse(connection.exists(null, null, null, example, false));
        assertTrue(connection.exists(null, null, null, null, false));
    }
    
    @Test
    public void Insert_Data() throws IOException {
        RDFUpdate insert = parse("INSERT DATA { rdf:type rdf:type rdf:Property }");
        insert.execute();
        
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, null, false));
    }
    
    @Test
    public void Insert_Data_Into() throws IOException{
        RDFUpdate insert = parse("INSERT DATA INTO <http://ex1.com> { rdf:type rdf:type rdf:Property }");
        insert.execute();
        
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, ex1, false));
    }    
    
    @Test
    public void Insert_Data_Into_Into() throws IOException{
        RDFUpdate insert = parse("INSERT DATA INTO <http://ex1.com> INTO <http://ex2.com> { rdf:type rdf:type rdf:Property }");
        insert.execute();
        
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, ex1, false));
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, ex2, false));
    }    
    
    @Test
    public void Insert() throws IOException{
        RDFUpdate insert = parse("INSERT { rdf:type rdf:type rdf:Property }");
        insert.execute();
        
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, null, false));
        assertFalse(connection.exists(RDF.type, RDF.type, RDF.Property, ex1, false));
    }
    
    @Test
    public void Insert_Where() throws IOException{
        RDFUpdate insert = parse("INSERT { ?s rdf:type <http://ex2.com> } WHERE { ?s ?p ?o }");
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, ex2, null, false));
    }
    
    @Test
    public void Insert_Into_Where() throws IOException{
        RDFUpdate insert = parse("INSERT INTO <http://ex1.com> { ?s rdf:type <http://ex2.com> } WHERE { ?s ?p ?o }");
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, ex2, ex1, false));
    }
    
    @Test
    public void Insert_Into_Into_Where() throws IOException{
        RDFUpdate insert = parse("INSERT INTO <http://ex1.com> INTO <http://ex2.com> { ?s rdf:type <http://example.com> } WHERE { ?s ?p ?o }");
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, example, ex1, false));
        assertTrue(connection.exists(RDFS.Resource, RDF.type, example, ex2, false));
    }
    
    @Test
    public void Load_From() throws IOException {
        RDFUpdate load = parse("LOAD <http://example.com>");
        load.execute();
        
        // TODO : assertions
    }
    
    @Test
    public void Load_From_Into() throws IOException{
        RDFUpdate load = parse("LOAD <http://example.com> INTO <http://example2.com>");
        load.execute();
        
        // TODO : assertions
    }
        
    @Test
    public void Modify() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_Empty_Delete() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE {} INSERT { ?s ?p2 ?o2 }");
        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_Empty_Insert() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT {}");
        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_URI() throws IOException{
        RDFUpdate modify = parse("MODIFY <http://ex1.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
        
        // TODO : assertions
    }  
    
    @Test
    public void Modify_URI_URI() throws IOException{
        RDFUpdate modify = parse("MODIFY <http://ex1.com> <http://ex2.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
        
        // TODO : assertions
    }  

    @Test
    public void Modify_Where() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s2 ?p2 ?o2 } WHERE { ?s3 ?p3 ?o3 }");
        modify.execute();
        
        // TODO : assertions
    }
    
    private RDFUpdate parse(String string) throws IOException {
        return new RDFUpdateImpl(connection, PREFIXES + string);
    }
    
}
