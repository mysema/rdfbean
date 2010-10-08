package com.mysema.rdfbean.sparql;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SPARQLServletTest {

    private static SPARQLServlet servlet = new SPARQLServlet();
    
    private static MockServletConfig config;
    
    private static MemoryRepository repository;
    
    private MockHttpServletRequest request = new MockHttpServletRequest();
    
    private MockHttpServletResponse response = new MockHttpServletResponse();
    
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
    public void Select() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        servlet.service(request, response);
        assertTrue(response.getContentAsString().contains("<sparql"));
        assertTrue(response.getContentAsString().contains("<head>"));
        assertTrue(response.getContentAsString().contains("<results>"));
        assertTrue(response.getContentAsString().contains("literal"));
    }
    
    @Test
    public void Select_as_JSON() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        request.setParameter("type", "json");
        servlet.service(request, response);
        assertTrue(response.getContentAsString().contains("head"));
        assertTrue(response.getContentAsString().contains("results"));
        assertTrue(response.getContentAsString().contains("literal"));
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
        // TODO : assertions
    }
    
    @Test
    public void Construct_as_NTriples() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        request.setParameter("type", "ntriples");
        servlet.service(request, response);
        // TODO : assertions
    }
}
