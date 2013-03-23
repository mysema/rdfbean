package com.mysema.rdfbean.sparql;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class JSONResultProducerTest {

    private static MemoryRepository repository;

    private RDFConnection connection;

    @BeforeClass
    public static void setUpClass() throws ServletException {
        repository = new MemoryRepository();
        repository.initialize();
        repository.load(Format.RDFXML, SPARQLImplicitLimitTest.class.getResourceAsStream("/foaf.rdf"), null, false);
    }

    @AfterClass
    public static void tearDownClass() {
        repository.close();
    }

    @Before
    public void setUp() {
        connection = repository.openConnection();
    }

    @After
    public void tearDown() {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void Ask() throws IOException {
        JSONResultProducer producer = new JSONResultProducer();
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, "ASK { ?s ?p ?o }");
        StringWriter writer = new StringWriter();
        producer.stream(query, writer);
        assertEquals("{\"head\":null,\"boolean\":true}", writer.toString());
    }

    @Test
    public void Select() throws IOException {
        JSONResultProducer producer = new JSONResultProducer();
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, "SELECT * WHERE { ?s ?p ?o } LIMIT 4");
        StringWriter writer = new StringWriter();
        producer.stream(query, writer);
        System.out.println(writer.toString());
        String response = writer.toString();
        assertTrue(response.startsWith("{\"head\":{\"vars\":[\"s\",\"p\",\"o\"]},\"results\":{\"bindings\":[{\"s\":"));
        assertTrue(response.endsWith("}}]}}"));
    }

}
