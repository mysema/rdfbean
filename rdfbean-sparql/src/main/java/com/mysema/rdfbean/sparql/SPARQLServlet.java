package com.mysema.rdfbean.sparql;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
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
public class SPARQLServlet extends HttpServlet{
    
    private static final long serialVersionUID = 5726683938555535282L;

    public static final String SPARQL_RESULTS_JSON = "application/sparql-results+json";
    
    public static final String SPARQL_RESULTS_XML = "application/sparql-results+xml";
    
    private final SPARQLResultProducer resultProducer = new SPARQLResultProducer();
    
    @Nullable
    private Repository repository;

    public SPARQLServlet(Repository repository) {
        this.repository = repository;
    }
    
    public SPARQLServlet() {
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (repository == null){
            repository = (Repository) config.getServletContext().getAttribute(Repository.class.getName());    
        }
                
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
                }else{
                    contentType = getAcceptedType(request, contentType);
                }
                response.setContentType(contentType);
                query.streamTriples(response.getWriter(), contentType);
                
            }else{
                String contentType = SPARQL_RESULTS_XML;
                if ("json".equals(type)){
                    contentType = SPARQL_RESULTS_JSON;
                }else{
                    contentType = getAcceptedType(request, contentType);
                }
                response.setContentType(contentType);
                if (contentType.equals(SPARQL_RESULTS_JSON)){
                    resultProducer.streamAsJSON(query, response.getWriter());
                }else{
                    XMLWriter writer = new XMLWriter(response.getWriter());
                    resultProducer.streamAsXML(query, writer);
                }    
            }
        }finally{
            connection.close();
        }        
    }
    
    private String getAcceptedType(HttpServletRequest request, String defaultType){
        String accept = request.getHeader("Accept");
        if (accept != null){
            return accept.contains(",") ? accept.substring(0, accept.indexOf(',')) : accept;
        }else{
            return defaultType;
        }
    }

}
