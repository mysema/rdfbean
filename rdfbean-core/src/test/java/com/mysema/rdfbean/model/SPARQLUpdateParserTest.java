package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class SPARQLUpdateParserTest {
        
    private static final UID ex2 = new UID("http://ex2.com");

    private static final UID ex1 = new UID("http://ex1.com");
    
    private static final UID example = new UID("http://example.com");
        
    @Test
    public void Prefixes() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("PREFIX dc: <http://purl.org/dc/elements/1.1/>\n");
        builder.append("INSERT { <http://example/egbook3> dc:title  \"This is an example title\" }");
        UpdateClause insert = parse(builder.toString());
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals(DC.NS, insert.getPrefixes().get("dc"));
    }
    
    @Test
    public void Clear() throws IOException{
//        CLEAR [ GRAPH <uri> ]
        UpdateClause clear = parse("CLEAR");
        assertEquals(UpdateClause.Type.CLEAR, clear.getType());
    }
    
    @Test
    public void Clear_Graph() throws IOException{
        UpdateClause clear = parse("CLEAR GRAPH <http://example.com>");
        assertEquals(UpdateClause.Type.CLEAR, clear.getType());
        assertEquals(example, clear.getSource());
    }

    @Test
    public void Create() throws IOException{
//        CREATE [ SILENT ] GRAPH <uri>
        UpdateClause create = parse("CREATE GRAPH <http://example.com>");
        assertEquals(UpdateClause.Type.CREATE, create.getType());
        assertFalse(create.isSilent());
    }
    
    @Test
    public void Create_Silent() throws IOException {
        UpdateClause create = parse("CREATE SILENT GRAPH <http://example.com>");
        assertEquals(UpdateClause.Type.CREATE, create.getType());
        assertTrue(create.isSilent());
    }
    
    @Test
    public void Delete_Data() throws IOException{
//        DELETE DATA [ FROM <uri> ]* { triples }
        UpdateClause delete = parse("DELETE DATA { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.DELETE, delete.getType());
        assertEquals("?s ?p ?o", delete.getTemplate());
    }
    
    @Test
    public void Delete_Data_From() throws IOException {
        UpdateClause delete = parse("DELETE DATA FROM <http://ex1.com> { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.DELETE, delete.getType());
        assertEquals(Arrays.asList(ex1), delete.getFrom());
        assertEquals("?s ?p ?o", delete.getTemplate());
    }
    
    @Test
    public void Delete_Data_From_FROM() throws IOException {
        UpdateClause delete = parse("DELETE DATA FROM <http://ex1.com> FROM <http://ex2.com> { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.DELETE, delete.getType());
        assertEquals(Arrays.asList(ex1, ex2), delete.getFrom());
        assertEquals("?s ?p ?o", delete.getTemplate());
    }
    
    @Test
    public void Delete_Where() throws IOException{
//        DELETE [ FROM <uri> ]* { template } [ WHERE { pattern } ]
        UpdateClause delete = parse("DELETE { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.DELETE, delete.getType());
        assertEquals("?s rdf:type rdfs:Class", delete.getTemplate());
        assertEquals("?s ?p ?o", delete.getPattern());
    }
    
    @Test
    public void Delete_From_Where() throws IOException{
        UpdateClause delete = parse("DELETE FROM <http://ex1.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.DELETE, delete.getType());
        assertEquals("?s rdf:type rdfs:Class", delete.getTemplate());
        assertEquals("?s ?p ?o", delete.getPattern());
        assertEquals(ex1, delete.getFrom().get(0));
    }
    
    @Test
    public void Delete_From_From_Where() throws IOException{
        UpdateClause delete = parse("DELETE FROM <http://ex1.com> FROM <http://ex2.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.DELETE, delete.getType());
        assertEquals("?s rdf:type rdfs:Class", delete.getTemplate());
        assertEquals("?s ?p ?o", delete.getPattern());
        assertEquals(ex1, delete.getFrom().get(0));
    }
    
    @Test
    public void Drop() throws IOException{
//        DROP [ SILENT ] GRAPH <uri>
        UpdateClause create = parse("DROP GRAPH <http://example.com>");
        assertEquals(UpdateClause.Type.DROP, create.getType());
        assertFalse(create.isSilent());
    }
    
    @Test
    public void Drop_Silent() throws IOException {
        UpdateClause create = parse("DROP SILENT GRAPH <http://example.com>");
        assertEquals(UpdateClause.Type.DROP, create.getType());
        assertTrue(create.isSilent());
    }
    
    @Test
    public void Insert_Data() throws IOException{
//        INSERT DATA [ INTO <uri> ]* { triples }
        UpdateClause insert = parse("INSERT DATA { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals("?s ?p ?o", insert.getTemplate());
    }
    
    @Test
    public void Insert_Data_Into() throws IOException{
//        INSERT DATA [ INTO <uri> ]* { triples }
        UpdateClause insert = parse("INSERT DATA INTO <http://ex1.com> { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals("?s ?p ?o", insert.getTemplate());
        assertEquals(ex1, insert.getInto().get(0));
    }    
    
    @Test
    public void Insert_Data_Into_Into() throws IOException{
//        INSERT DATA [ INTO <uri> ]* { triples }
        UpdateClause insert = parse("INSERT DATA INTO <http://ex1.com> INTO <http://ex2.com> { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals("?s ?p ?o", insert.getTemplate());
        assertEquals(Arrays.asList(ex1, ex2), insert.getInto());
    }    
    
    @Test
    public void Insert() throws IOException{
        UpdateClause insert = parse("INSERT { ?s rdf:type rdfs:Class }");
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals("?s rdf:type rdfs:Class", insert.getTemplate());
    }
    
    @Test
    public void Insert_Where() throws IOException{
//        INSERT [ INTO <uri> ]* { template } [ WHERE { pattern } ]
        UpdateClause insert = parse("INSERT { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals("?s rdf:type rdfs:Class", insert.getTemplate());
        assertEquals("?s ?p ?o", insert.getPattern());
    }
    
    @Test
    public void Insert_Into_Where() throws IOException{
//        INSERT [ INTO <uri> ]* { template } [ WHERE { pattern } ]
        UpdateClause insert = parse("INSERT INTO <http://ex1.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals("?s rdf:type rdfs:Class", insert.getTemplate());
        assertEquals("?s ?p ?o", insert.getPattern());
        assertEquals(ex1, insert.getInto().get(0));
    }
    
    @Test
    public void Insert_Into_Into_Where() throws IOException{
//        INSERT [ INTO <uri> ]* { template } [ WHERE { pattern } ]
        UpdateClause insert = parse("INSERT INTO <http://ex1.com> INTO <http://ex2.com> { ?s rdf:type rdfs:Class } WHERE { ?s ?p ?o }");
        assertEquals(UpdateClause.Type.INSERT, insert.getType());
        assertEquals("?s rdf:type rdfs:Class", insert.getTemplate());
        assertEquals("?s ?p ?o", insert.getPattern());
        assertEquals(Arrays.asList(ex1, ex2), insert.getInto());
    }
    
    @Test
    public void Load_From() throws IOException {
//        LOAD <remoteURI> [ INTO <uri> ]        
        UpdateClause load = parse("LOAD <http://example.com>");
        assertEquals(UpdateClause.Type.LOAD, load.getType());
        assertEquals(example, load.getSource());
        assertNull(load.getTarget());
    }
    
    @Test
    public void Load_From_Into() throws IOException{
        UpdateClause load = parse("LOAD <http://example.com> INTO <http://example2.com>");
        assertEquals(UpdateClause.Type.LOAD, load.getType());
        assertEquals(example, load.getSource());
        assertEquals(new UID("http://example2.com"), load.getTarget());
    }
        
    @Test
    public void Modify() throws IOException{
//        MODIFY [ <uri> ]* DELETE { template } INSERT { template } [ WHERE { pattern } ]
        UpdateClause modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        assertEquals(UpdateClause.Type.MODIFY, modify.getType());
        assertEquals("?s ?p ?o", modify.getDelete());
        assertEquals("?s ?p2 ?o2", modify.getInsert());
    }    
    
    @Test
    public void Modify_Empty_Delete() throws IOException{
        UpdateClause modify = parse("MODIFY DELETE {} INSERT { ?s ?p2 ?o2 }");
        assertEquals(UpdateClause.Type.MODIFY, modify.getType());
        assertNull(modify.getDelete());
        assertEquals("?s ?p2 ?o2", modify.getInsert());
    }    
    
    @Test
    public void Modify_Empty_Insert() throws IOException{
        UpdateClause modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT {}");
        assertEquals(UpdateClause.Type.MODIFY, modify.getType());
        assertEquals("?s ?p ?o", modify.getDelete());
        assertNull(modify.getInsert());
    }    
    
    @Test
    public void Modify_URI() throws IOException{
        UpdateClause modify = parse("MODIFY <http://ex1.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        assertEquals(UpdateClause.Type.MODIFY, modify.getType());
        assertEquals("?s ?p ?o", modify.getDelete());
        assertEquals("?s ?p2 ?o2", modify.getInsert());
        assertEquals(ex1, modify.getInto().get(0));
    }  
    
    @Test
    public void Modify_URI_URI() throws IOException{
        UpdateClause modify = parse("MODIFY <http://ex1.com> <http://ex2.com> DELETE { ?s ?p ?o } INSERT { ?s ?p2 ?o2 }");
        assertEquals(UpdateClause.Type.MODIFY, modify.getType());
        assertEquals("?s ?p ?o", modify.getDelete());
        assertEquals("?s ?p2 ?o2", modify.getInsert());
        assertEquals(Arrays.asList(ex1, ex2), modify.getInto());
    }  

    @Test
    public void Modify_Where() throws IOException{
        UpdateClause modify = parse("MODIFY DELETE { ?s ?p ?o } INSERT { ?s2 ?p2 ?o2 } WHERE { ?s3 ?p3 ?o3 }");
        assertEquals(UpdateClause.Type.MODIFY, modify.getType());
        assertEquals("?s ?p ?o", modify.getDelete());
        assertEquals("?s2 ?p2 ?o2", modify.getInsert());
        assertEquals("?s3 ?p3 ?o3", modify.getPattern());
    }
    
    private UpdateClause parse(String string) throws IOException {
        return new SPARQLUpdateParser().parse(string);
    }
    
}
