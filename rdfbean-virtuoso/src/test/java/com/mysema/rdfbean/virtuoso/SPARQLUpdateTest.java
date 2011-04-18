package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.DC;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.SPARQLUpdate;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.UpdateLanguage;

public class SPARQLUpdateTest extends AbstractConnectionTest{

    private static final String PREFIXES = "PREFIX rdf: <"+RDF.NS+">\nPREFIX rdfs: <"+RDFS.NS+">\n";
    
    @Override
    @Before
    public void setUp(){
        super.setUp();
        
        connection.remove(null, null, null, example);
        connection.remove(null, null, null, ex1);
        connection.remove(null, null, null, ex2);
        
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class)));
    }
    
    @Test
    public void Prefixes() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("PREFIX dc: <http://purl.org/dc/elements/1.1/>\n");
        builder.append("INSERT INTO <http://example.com> { <http://example/egbook3> dc:title  \"This is an example title\" }");
        SPARQLUpdate insert = parse(builder.toString());
        insert.execute();
        
        assertTrue(connection.exists(new UID("http://example/egbook3"), DC.title, null, null, false));
    }
    
    @Test
    @Ignore // FIXME
    public void Clear() throws IOException{
        SPARQLUpdate clear = parse("CLEAR");
        clear.execute();
        
        assertFalse(connection.exists(null, null, null, null, false));
    }
    
    @Test
    public void Clear_Graph() throws IOException{
        connection.update(null, Arrays.asList(
                new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1),
                new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex2)));
        
        SPARQLUpdate clear = parse("CLEAR GRAPH <http://ex1.com>");
        clear.execute();
        
        assertFalse(connection.exists(null, null, null, ex1, false));
        assertTrue(connection.exists(null, null, null, ex2, false));
        
    }

    @Test
    @Ignore
    public void Create() throws IOException{
        SPARQLUpdate create = parse("CREATE GRAPH <http://example.com>");
        create.execute();
        
        // no effect
    }
    
    @Test
    public void Create_Silent() throws IOException {
        SPARQLUpdate create = parse("CREATE SILENT GRAPH <http://example.com>");
        create.execute();
        
        // no effect
    }
    
    @Test
    public void Delete_Data() throws IOException{
        SPARQLUpdate delete = parse("DELETE DATA { rdfs:Resource rdf:type rdfs:Class }");
        delete.execute();
        
        assertFalse(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, null, false));
    }
    
    @Test
    public void Delete_Data_From() throws IOException {
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        
        SPARQLUpdate delete = parse("DELETE DATA FROM <http://ex1.com> { rdfs:Resource rdf:type rdfs:Class }");
        delete.execute();
        
        assertFalse(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, ex1, false));
        assertTrue(connection.exists(RDFS.Resource, RDF.type, RDFS.Class, null, false));
    }
    
    @Test
    public void Delete_Where_No_Match() throws IOException{
        SPARQLUpdate delete = parse("DELETE { ?s rdf:type <http://example.com> } WHERE { ?s ?p ?o }");
        delete.execute();
        
        assertTrue(connection.exists(null, null, null, null, false));                
    }
    
    @Test
    public void Delete_Where_Matches() throws IOException{
        parse("DELETE { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }").execute();
        
        assertFalse(connection.exists(null, RDF.type, RDFS.Class, new UID(TEST.NS), false));
    }
    
    @Test
    public void Delete_From_Where() throws IOException{
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, ex1)));
        
        SPARQLUpdate delete = parse("DELETE FROM <http://ex1.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        delete.execute();
        
        assertFalse(connection.exists(null, null, null, ex1, false));
        assertTrue(connection.exists(null, null, null, null, false));    
    }
        
    @Test
    public void Drop() throws IOException{
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, example)));
        
        SPARQLUpdate create = parse("DROP GRAPH <http://example.com>");
        create.execute();
        
        assertFalse(connection.exists(null, null, null, example, false));
        assertTrue(connection.exists(null, null, null, null, false));
    }
    
    @Test
    public void Drop_Silent() throws IOException {
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class, example)));
        
        SPARQLUpdate create = parse("DROP SILENT GRAPH <http://example.com>");
        create.execute();
        
        assertFalse(connection.exists(null, null, null, example, false));
        assertTrue(connection.exists(null, null, null, null, false));
    }
    
    @Test
    public void Insert_Data() throws IOException {
        SPARQLUpdate insert = parse("INSERT DATA { rdf:type rdf:type rdf:Property }");
        insert.execute();
        
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, null, false));
    }
    
    @Test
    public void Insert_Data_Into() throws IOException{
        SPARQLUpdate insert = parse("INSERT DATA INTO <http://ex1.com> { rdf:type rdf:type rdf:Property }");
        insert.execute();
        
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, ex1, false));
    }    
    
    
    @Test
    public void Insert() throws IOException{
        SPARQLUpdate insert = parse("INSERT { rdf:type rdf:type rdf:Property }");
        insert.execute();
        
        assertTrue(connection.exists(RDF.type, RDF.type, RDF.Property, null, false));
        assertFalse(connection.exists(RDF.type, RDF.type, RDF.Property, ex1, false));
    }
    
    @Test
    public void Insert_Where() throws IOException{
        SPARQLUpdate insert = parse("INSERT { ?s rdf:type <http://ex2.com> } WHERE { ?s ?p ?o }");
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, ex2, null, false));
    }
    
    @Test
    public void Insert_Into_Where() throws IOException{
        SPARQLUpdate insert = parse("INSERT INTO <http://ex1.com> { ?s rdf:type <http://ex2.com> } WHERE { ?s ?p ?o }");
        insert.execute();
        
        assertTrue(connection.exists(RDFS.Resource, RDF.type, ex2, ex1, false));
    }
        
    @Test
    public void Load_From() throws IOException {
        SPARQLUpdate load = parse("LOAD <http://example.com>");
        load.execute();
        
        // TODO : assertions
    }
    
    @Test
    public void Load_From_Into() throws IOException{
        SPARQLUpdate load = parse("LOAD <http://example.com> INTO <http://example2.com>");
        load.execute();
        
        // TODO : assertions
    }
        
    @Test
    public void Modify() throws IOException{
        SPARQLUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_Empty_Delete() throws IOException{
        SPARQLUpdate modify = parse("MODIFY DELETE {} INSERT { ?s ?p2 ?o2 }");
        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_Empty_Insert() throws IOException{
        SPARQLUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT {}");
        modify.execute();
        
        // TODO : assertions
    }    
    
    @Test
    public void Modify_URI() throws IOException{
        SPARQLUpdate modify = parse("MODIFY <http://ex1.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
        
        // TODO : assertions
    }  
    
    @Test
    public void Modify_Where() throws IOException{
        SPARQLUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s2 ?p2 ?o2 } WHERE { ?s3 ?p3 ?o3 }");
        modify.execute();
        
        // TODO : assertions
    }
    
    private SPARQLUpdate parse(String string) throws IOException {
//        return new RDFUpdateImpl(connection, PREFIXES + string);
        return connection.createUpdate(UpdateLanguage.SPARQL_UPDATE, PREFIXES + string);
    }
    
}
