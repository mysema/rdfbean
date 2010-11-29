package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.SPARQLQueryBuilder;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SPARQLQueryBuilderTest {

    private static final Repository repository = new MemoryRepository();

    private final SPARQLQueryBuilder qry = new SPARQLQueryBuilder();

    @BeforeClass
    public static void setUpClass(){
        repository.initialize();
    }

    @AfterClass
    public static void tearDownClass(){
        repository.close();
    }

    @Test
    public void Select_From(){
        qry.select("?s", "?p", "?o").from(new UID(TEST.NS)).where("?s ?p ?o");

        assertEquals("SELECT ?s ?p ?o\n" +
        	"FROM <http://semantics.mysema.com/test#>\n" +
        	"WHERE {\n  ?s ?p ?o . }\n", qry.toString());
        execute();
    }

    @Test
    public void Select_From_Named(){
        qry.select("?s", "?p", "?o").fromNamed(new UID(TEST.NS)).where("?s ?p ?o");

        assertEquals("SELECT ?s ?p ?o\n" +
                "FROM NAMED <http://semantics.mysema.com/test#>\n" +
        	"WHERE {\n  ?s ?p ?o . }\n", qry.toString());
        execute();
    }


    @Test
    public void Select_Where_Order(){
        qry.select("?s", "?p", "?o").where("?s ?p ?o").orderBy("?s", "?p", "?o");

        assertEquals("SELECT ?s ?p ?o\nWHERE {\n  ?s ?p ?o . }\nORDER BY ?s ?p ?o\n", qry.toString());
        execute();
    }

    @Test
    public void Select_Where_Limit(){
        qry.select("?s", "?p", "?o").where("?s ?p ?o").limit(5);

        assertEquals("SELECT ?s ?p ?o\nWHERE {\n  ?s ?p ?o . }\nLIMIT 5\n", qry.toString());
        execute();
    }

    @Test
    public void Select_Where(){
        qry.select("?s", "?p", "?o").where("?s ?p ?o");

        assertEquals("SELECT ?s ?p ?o\nWHERE {\n  ?s ?p ?o . }\n", qry.toString());
        execute();
    }

    @Test
    public void Select_Where_Quad(){
        qry.select("?s", "?p", "?o").where("?s", "?p", "?o", new UID(TEST.NS));

        assertEquals("SELECT ?s ?p ?o\nWHERE {\n  GRAPH <http://semantics.mysema.com/test#> { ?s ?p ?o } . }\n", qry.toString());
        execute();
    }

    @Test
    public void Select_Where_Graph_Quad(){
        qry.select("?s", "?p", "?o").graph(new UID(TEST.NS), "?s ?p ?o");

        assertEquals("SELECT ?s ?p ?o\nWHERE {\n  GRAPH <http://semantics.mysema.com/test#> { ?s ?p ?o } . }\n", qry.toString());
        execute();
    }

    @Test
    public void Select_Where_with_URIs(){
        UID book1 = new UID("http://example.org/book/book1");
        UID title = new UID("http://purl.org/dc/elements/1.1/title");
        qry.select("?title").where(book1, title, "?title");

        assertEquals("SELECT ?title\nWHERE {\n" +
            "  <http://example.org/book/book1> <http://purl.org/dc/elements/1.1/title> ?title . }\n", qry.toString());
        execute();
    }

    @Test
    public void Select_Where_Filter(){
        qry.select("?s", "?p", "?o").where("?s ?p ?o").filter("?o = 1");

        assertEquals("SELECT ?s ?p ?o\nWHERE {\n  ?s ?p ?o . \n  FILTER (?o = 1) . }\n", qry.toString());
        execute();
    }

    @Test
    public void Localized_Literal(){
        qry.select("?s").where("?s", RDFS.label, new LIT("cat", Locale.ENGLISH));

        assertEquals("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
        	"SELECT ?s\nWHERE {\n  ?s rdfs:label \"cat\"@en . }\n", qry.toString());
        execute();
    }

    @Test
    public void Typed_Literal(){
        qry.select("?s").where("?s", new UID(TEST.NS, "test"), new LIT("123", XSD.integerType));

        assertEquals("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
        	"SELECT ?s\nWHERE {\n  ?s <http://semantics.mysema.com/test#test> \"123\"^^xsd:integer . }\n", qry.toString());
        execute();
    }

    @Test
    public void Optional(){
        qry.select("?s", "?lit", "?type")
            .where("?s", new UID(TEST.NS, "test"), "?lit")
            .optional("?s", RDF.type, "?type");

        assertEquals("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        	"SELECT ?s ?lit ?type\n" +
        	"WHERE {\n" +
        	"  ?s <http://semantics.mysema.com/test#test> ?lit . \n" +
        	"  OPTIONAL { ?s rdf:type ?type } . }\n", qry.toString());
        execute();
    }

    private void execute() {
        RDFConnection conn = repository.openConnection();
        try{
            conn.createQuery(QueryLanguage.SPARQL, qry.toString()).getVariables();
        }finally{
            conn.close();
        }
    }


}
