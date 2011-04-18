package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.DC;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.RDFUpdate;
import com.mysema.rdfbean.model.RDFUpdateImpl;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class RDFUpdateTest extends AbstractConnectionTest{
    
    private static final UID ex1 = new UID("http://ex1.com");
    
    private static final UID ex2 = new UID("http://ex2.com");

    private static final String PREFIXES = "PREFIX rdf: <"+RDF.NS+">\nPREFIX rdfs: <"+RDFS.NS+">\n";
    
    @Override
    @Before
    public void before(){
        super.before();
        connection.update(null, Collections.singleton(new STMT(RDFS.Resource, RDF.type, RDFS.Class)));
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
    }
    
    @Test
    public void Create_Silent() throws IOException {
        RDFUpdate create = parse("CREATE SILENT GRAPH <http://example.com>");
        create.execute();
    }
    
    @Test
    public void Delete_Data() throws IOException{
        RDFUpdate delete = parse("DELETE DATA { ?s ?p ?o }");
        delete.execute();
    }
    
    @Test
    public void Delete_Data_From() throws IOException {
        RDFUpdate delete = parse("DELETE DATA FROM <http://ex1.com> { ?s ?p ?o }");
        delete.execute();
    }
    
    @Test
    public void Delete_Data_From_FROM() throws IOException {
        RDFUpdate delete = parse("DELETE DATA FROM <http://ex1.com> FROM <http://ex2.com> { ?s ?p ?o }");
        delete.execute();
    }
    
    @Test
    public void Delete_Where() throws IOException{
        RDFUpdate delete = parse(PREFIXES + "DELETE { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        delete.execute();
    }
    
    @Test
    public void Delete_From_Where() throws IOException{
        RDFUpdate delete = parse(PREFIXES + "DELETE FROM <http://ex1.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        delete.execute();
    }
    
    @Test
    public void Delete_From_From_Where() throws IOException{
        RDFUpdate delete = parse(PREFIXES + "DELETE FROM <http://ex1.com> FROM <http://ex2.com> { ?s rdf:type rdfs:Class } " +
                "WHERE { ?s ?p ?o }");
        delete.execute();
    }
    
    @Test
    public void Drop() throws IOException{
        RDFUpdate create = parse("DROP GRAPH <http://example.com>");
        create.execute();
    }
    
    @Test
    public void Drop_Silent() throws IOException {
        RDFUpdate create = parse("DROP SILENT GRAPH <http://example.com>");
        create.execute();
    }
    
    @Test
    public void Insert_Data() throws IOException {
        RDFUpdate insert = parse("INSERT DATA { ?s ?p ?o }");
        insert.execute();
    }
    
    @Test
    public void Insert_Data_Into() throws IOException{
        RDFUpdate insert = parse("INSERT DATA INTO <http://ex1.com> { ?s ?p ?o }");
        insert.execute();
    }    
    
    @Test
    public void Insert_Data_Into_Into() throws IOException{
        RDFUpdate insert = parse("INSERT DATA INTO <http://ex1.com> INTO <http://ex2.com> { ?s ?p ?o }");
        insert.execute();
    }    
    
    @Test
    public void Insert() throws IOException{
        RDFUpdate insert = parse(PREFIXES + "INSERT { ?s rdf:type rdfs:Class }");
        insert.execute();
    }
    
    @Test
    public void Insert_Where() throws IOException{
        RDFUpdate insert = parse(PREFIXES + "INSERT { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        insert.execute();
    }
    
    @Test
    public void Insert_Into_Where() throws IOException{
        RDFUpdate insert = parse(PREFIXES + "INSERT INTO <http://ex1.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        insert.execute();
    }
    
    @Test
    public void Insert_Into_Into_Where() throws IOException{
        RDFUpdate insert = parse(PREFIXES + "INSERT INTO <http://ex1.com> INTO <http://ex2.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        insert.execute();
    }
    
    @Test
    public void Load_From() throws IOException {
        RDFUpdate load = parse("LOAD <http://example.com>");
        load.execute();
    }
    
    @Test
    public void Load_From_Into() throws IOException{
        RDFUpdate load = parse("LOAD <http://example.com> INTO <http://example2.com>");
        load.execute();
    }
        
    @Test
    public void Modify() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
    }    
    
    @Test
    public void Modify_Empty_Delete() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE {} INSERT { ?s ?p2 ?o2 }");
        modify.execute();
    }    
    
    @Test
    public void Modify_Empty_Insert() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT {}");
        modify.execute();
    }    
    
    @Test
    public void Modify_URI() throws IOException{
        RDFUpdate modify = parse("MODIFY <http://ex1.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
    }  
    
    @Test
    public void Modify_URI_URI() throws IOException{
        RDFUpdate modify = parse("MODIFY <http://ex1.com> <http://ex2.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        modify.execute();
    }  

    @Test
    public void Modify_Where() throws IOException{
        RDFUpdate modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s2 ?p2 ?o2 } WHERE { ?s3 ?p3 ?o3 }");
        modify.execute();
    }
    
    private RDFUpdate parse(String string) throws IOException {
        return new RDFUpdateImpl(connection, string);
    }
    
}
