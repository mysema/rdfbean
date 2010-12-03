package com.mysema.rdfbean.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SPARQLImplicitLimitTest {

    private static SPARQLServlet servlet = new SPARQLServlet();

    private static MemoryRepository repository;

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeClass
    public static void setUpClass() throws ServletException{
        repository = new MemoryRepository();
        repository.initialize();
        repository.load(Format.RDFXML, SPARQLImplicitLimitTest.class.getResourceAsStream("/foaf.rdf"), null, false);

        servlet = new SPARQLServlet(repository, 10);
    }

    @Test
    public void Implicit_Limit_with_Construct() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ORDER BY ?s ?p ?o");
        request.addHeader("Accept", Format.NTRIPLES.getMimetype());
        servlet.service(request, response);
        String content = response.getContentAsString();
        assertEquals(10, content.split("\n").length);
    }
    
    @Test
    public void Implicit_Limit_with_Select() throws ServletException, IOException{
        request.setParameter("query", "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        request.addHeader("Accept", Format.NTRIPLES.getMimetype());
        servlet.service(request, response);
    }
    
    @Test
    public void Implicit_Limit_with_Ask() throws ServletException, IOException{
        request.setParameter("query", "ASK WHERE { ?s ?p ?o }");
        request.addHeader("Accept", Format.NTRIPLES.getMimetype());
        servlet.service(request, response);
        String content = response.getContentAsString();
        assertTrue(content.contains("<boolean>true</boolean>"));
    }
    
    @Test
    public void Explicit_High_Limit_Overriden() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ORDER BY ?s ?p ?o LIMIT 30");
        request.addHeader("Accept", Format.NTRIPLES.getMimetype());
        servlet.service(request, response);
        String content = response.getContentAsString();
        assertEquals(10, content.split("\n").length);
    }

    @Test
    public void Explicit_Low_Limit() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ORDER BY ?s ?p ?o LIMIT 5");
        request.addHeader("Accept", Format.NTRIPLES.getMimetype());
        servlet.service(request, response);
        String content = response.getContentAsString();
        assertEquals(5, content.split("\n").length);
    }

}
