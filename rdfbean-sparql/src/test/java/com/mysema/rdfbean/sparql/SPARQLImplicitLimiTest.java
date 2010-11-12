package com.mysema.rdfbean.sparql;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SPARQLImplicitLimiTest {

    private static SPARQLServlet servlet = new SPARQLServlet();

    private static MemoryRepository repository;

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeClass
    public static void setUpClass() throws ServletException{
        repository = new MemoryRepository();
        repository.initialize();
        repository.load(Format.RDFXML, SPARQLServletTest.class.getResourceAsStream("/foaf.rdf"), null, false);

        servlet = new SPARQLServlet(repository, 10);
    }

    @Test
    public void Implicit_Limit() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ORDER BY ?s ?p ?o");
        request.addHeader("Accept", Format.NTRIPLES.getMimetype());
        servlet.service(request, response);
        String content = response.getContentAsString();
        assertEquals(10, content.split("\n").length);
    }

    @Test
    public void Explicit_Limit() throws ServletException, IOException{
        request.setParameter("query", "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ORDER BY ?s ?p ?o LIMIT 30");
        request.addHeader("Accept", Format.NTRIPLES.getMimetype());
        servlet.service(request, response);
        String content = response.getContentAsString();
        assertEquals(30, content.split("\n").length);
    }

}
