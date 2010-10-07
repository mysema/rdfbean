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


/**
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
            if (query.getResultType() == SPARQLQuery.ResultType.TUPLES){
                if ("json".equals(request.getParameter("type"))){
                    response.setContentType("application/sparql-results+json");
                    resultProducer.streamJSONResults(query, response.getWriter());
                }else{
                    response.setContentType("application/sparql-results+xml");
                    XMLWriter writer = new XMLWriter(response.getWriter());
                    resultProducer.streamXMLResults(query, writer);
                }
                
            }else{
                // TODO : return triple results
            }
        }finally{
            connection.close();
        }
        
    }

    

}
