package com.mysema.rdfbean.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SPARQLServletTest {

    private static SPARQLServlet servlet = new SPARQLServlet();

    private static MockServletConfig config;

    private static MemoryRepository repository;

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeClass
    public static void setUpClass() throws ServletException{
        repository = new MemoryRepository();
        repository.initialize();
        repository.load(Format.RDFXML, SPARQLServletTest.class.getResourceAsStream("/foaf.rdf"), null, false);
        config = new MockServletConfig();
        config.getServletContext().setAttribute(Repository.class.getName(), repository);

        servlet.init(config);
    }

    @AfterClass
    public static void tearDownClass(){
        repository.close();
    }

    @Test
    public void Ask() throws ServletException, IOException{
        request.setParameter("query", "ASK { ?s ?p ?o }");
        servlet.service(request, response);
        System.out.println(response.getContentAsString());
        assertTrue(response.getContentAsString().contains("<sparql"));
        assertTrue(response.getContentAsString().contains("<head/>"));
        assertTrue(response.getContentAsString().contains("<results><boolean>true</boolean></results>"));
    }

    @Test
    public void Ask_as_JSON() throws ServletException, IOException{
        request.setParameter("query", "ASK { ?s ?p ?o }");
        request.setParameter("type", "json");
        servlet.service(request, response);
        System.out.println(response.getContentAsString());
        assertTrue(response.getContentAsString().contains("head"));
        assertTrue(response.getContentAsString().contains("boolean"));
        assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void Construct() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        servlet.service(request, response);
        assertTrue(response.getContentAsString().contains("<rdf:RDF"));
        assertTrue(response.getContentAsString().contains(RDF.NS));
    }

    @Test
    public void Construct_as_Turtle() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        request.setParameter("type", "turtle");
        servlet.service(request, response);
        assertTrue(!response.getContentAsString().contains("<rdf:RDF"));
    }

    @Test
    public void Construct_as_Turtle_via_Accept() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        request.addHeader("Accept", Format.TURTLE.getMimetype()+", "+Format.RDFXML.getMimetype());
        servlet.service(request, response);
        assertTrue(!response.getContentAsString().contains("<rdf:RDF"));
    }

    @Test
    public void Construct_as_NTriples() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        request.setParameter("type", "ntriples");
        servlet.service(request, response);
        assertTrue(!response.getContentAsString().contains("<rdf:RDF"));
    }

    @Test
    public void Construct_with_Html_Accept() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        request.addHeader("Accept", "text/html");
        servlet.service(request, response);
        assertEquals(Format.RDFXML.getMimetype(), response.getContentType());
    }

    @Test
    public void Select() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        servlet.service(request, response);
        assertTrue(response.getContentAsString().contains("<sparql"));
        assertTrue(response.getContentAsString().contains("<head>"));
        assertTrue(response.getContentAsString().contains("<results>"));
        assertTrue(response.getContentAsString().contains("literal"));
        assertEquals(SPARQLServlet.SPARQL_RESULTS_XML, response.getContentType());
    }

    @Test
    public void Select_with_MaxQueryTime() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        SPARQLServlet servlet2 = new SPARQLServlet(repository, null, 1);
        servlet2.service(request, response);

        assertTrue(response.getContentAsString().contains("<sparql"));
        assertTrue(response.getContentAsString().contains("<head>"));
        assertTrue(response.getContentAsString().contains("<results>"));
        assertTrue(response.getContentAsString().contains("literal"));
        assertEquals(SPARQLServlet.SPARQL_RESULTS_XML, response.getContentType());
    }

    @Test
    public void Select_with_Html_Accept() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        request.addHeader("Accept", "text/html");
        servlet.service(request, response);
        assertEquals(SPARQLServlet.SPARQL_RESULTS_XML, response.getContentType());
    }

    @Test
    public void Select_as_JSON() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        request.setParameter("type", "json");
        servlet.service(request, response);
        assertTrue(response.getContentAsString().contains("head"));
        assertTrue(response.getContentAsString().contains("results"));
        assertTrue(response.getContentAsString().contains("literal"));
        assertEquals(SPARQLServlet.SPARQL_RESULTS_JSON, response.getContentType());
    }
    
    @Test
    public void Select_as_JSON_with_JSONP() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        request.setParameter("type", "json");
        request.setParameter("callback", "handleResponse");
        servlet.service(request, response);
        assertTrue(response.getContentAsString().startsWith("handleResponse("));
        assertTrue(response.getContentAsString().endsWith(")"));
        assertTrue(response.getContentAsString().contains("head"));
        assertTrue(response.getContentAsString().contains("results"));
        assertTrue(response.getContentAsString().contains("literal"));
        assertEquals(SPARQLServlet.SPARQL_RESULTS_JSON, response.getContentType());
    }

    @Test
    public void Select_as_JSON_via_Accept() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        request.addHeader("Accept", SPARQLServlet.SPARQL_RESULTS_JSON);
        servlet.service(request, response);
        assertTrue(response.getContentAsString().contains("head"));
        assertTrue(response.getContentAsString().contains("results"));
        assertTrue(response.getContentAsString().contains("literal"));
        assertEquals(SPARQLServlet.SPARQL_RESULTS_JSON, response.getContentType());
    }

    @Test
    public void Select_with_Optional_Bindings_as_JSON() throws ServletException, IOException{
        StringBuilder query = new StringBuilder();
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        query.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>\n");
        query.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
        query.append("SELECT ?c ?c2 ?domain " +
        	"WHERE { ?c rdf:type rdfs:Class . " +
        	"OPTIONAL { ?c owl:disjointWith ?c2 . } " +
        	"OPTIONAL { ?c rdfs:domain ?domain } }");

        request.setParameter("query", query.toString());
        request.addHeader("Accept", SPARQLServlet.SPARQL_RESULTS_JSON);
        servlet.service(request, response);
        System.out.println(response.getContentAsString());
    }

}
