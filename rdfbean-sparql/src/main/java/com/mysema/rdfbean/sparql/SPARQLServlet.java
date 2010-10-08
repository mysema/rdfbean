package com.mysema.rdfbean.sparql;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mysema.commons.fluxml.XMLWriter;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.io.Format;


/**
 * SPARQLServlet provides a Servlet based SPARQL HTTP access point for RDFBean repositories
 * 
 * @author tiwe
 *
 */
public class SPARQLServlet implements Servlet{
    
    private final SPARQLResultProducer resultProducer = new SPARQLResultProducer();
    
    private ServletConfig config;
    
    private Repository repository;
    
    @Override
    public void destroy() {
        
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return "RDFBean SPARQL service";
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        repository = (Repository) config.getServletContext().getAttribute(Repository.class.getName());        
    }
    
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        String queryString = request.getParameter("query");        
        if (queryString == null){
            response.sendError(500, "No query given");
            return;
        }
        
        RDFConnection connection = repository.openConnection();
        try{
            SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, queryString);
            String type = request.getParameter("type");
            if (query.getResultType() == SPARQLQuery.ResultType.TRIPLES){
                String contentType = Format.RDFXML.getMimetype();
                if ("turtle".equals(type)){
                    contentType = Format.TURTLE.getMimetype();
                }else if ("ntriples".equals(type)){
                    contentType = Format.NTRIPLES.getMimetype();
                }
                response.setContentType(contentType);
                query.streamTriples(response.getWriter(), contentType);
                
            }else{
                if ("json".equals(type)){
                    response.setContentType("application/sparql-results+json");
                    resultProducer.streamAsJSON(query, response.getWriter());
                }else{
                    response.setContentType("application/sparql-results+xml");
                    XMLWriter writer = new XMLWriter(response.getWriter());
                    resultProducer.streamAsXML(query, writer);
                }    
            }
        }finally{
            connection.close();
        }
        
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}
