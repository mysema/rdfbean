package com.mysema.rdfbean.sparql;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mysema.commons.fluxml.XMLWriter;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.SPARQLQuery;


public class SPARQLServlet implements Servlet{

    private static final String SPARQL_NS = "http://www.w3.org/2005/sparql-results#";
    
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
        RDFConnection connection = repository.openConnection();
        try{
            SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, queryString);
            if (query.getResultType() == SPARQLQuery.ResultType.TUPLES){
                response.setContentType("application/sparql-results+xml");
                XMLWriter writer = new XMLWriter(response.getWriter());
                writer.begin("sparql");
                writer.attribute("xmlns", SPARQL_NS);
                writer.begin("head");
                for (String var : query.getVariables()){
                    writer.begin("variable").attribute("name", var).end("variable");
                }                
                writer.end("head");
                writer.begin("results");
                CloseableIterator<Map<String,NODE>> rows = query.getTuples();
                while (rows.hasNext()){
                    Map<String,NODE> row = rows.next();
                    writer.begin("result");
                    for (Map.Entry<String, NODE> entry : row.entrySet()){
                        writer.begin("binding").attribute("name", entry.getKey());
                        String type;
                        switch (entry.getValue().getNodeType()){
                            case BLANK: type = "bnode"; break;
                            case URI: type = "uri"; break;
                            case LITERAL: type = "literal"; break;
                            default: type = "null";
                        }
                        writer.begin(type);
                        // TODO : lang
                        // TODO : datatype
                        writer.print(entry.getValue().getValue());
                        writer.end(type);
                        writer.end("binding");
                    }
                    writer.end("result");
                }
                writer.end("results");
                writer.end("sparql");
            }else{
                // TODO : return triple results
            }
        }finally{
            connection.close();
        }
        
    }

}
