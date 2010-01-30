/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.parser.QueryParserRegistry;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.GraphResult;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

/**
 * DirectQuery provides integration of programmatically constructed queries into Repository connection
 *
 * @author tiwe
 * @version $Id$
 */
public class DirectQuery {
    
    private static ThreadLocal<QueryModel> QUERY_HOLDER = new ThreadLocal<QueryModel>();    
    
    private static final QueryLanguage DIRECTQUERY = new QueryLanguage("DIRECTQUERY");    
    
    private static final QueryParser DIRECTQUERY_PARSER = new QueryParser(){
        @Override
        /**
         * Returns the threadbound query, ignores the parameters of the method invocation
         */
        public QueryModel parseQuery(String queryStr, String baseURI)throws MalformedQueryException {
            return QUERY_HOLDER.get();
        }        
    };

    static{
        QueryParserRegistry.getInstance().add(new QueryParserFactory(){
            @Override
            public QueryParser getParser() {
                return DIRECTQUERY_PARSER;
            }
            @Override
            public QueryLanguage getQueryLanguage() {
                return DIRECTQUERY;
            }            
        });
    }

    public static TupleResult query(RepositoryConnection connection, TupleQueryModel tupleQueryModel, boolean includeInferred) throws StoreException, MalformedQueryException{
        QUERY_HOLDER.set(tupleQueryModel);
        TupleQuery tupleQuery = connection.prepareTupleQuery(DirectQuery.DIRECTQUERY, "");                      
        tupleQuery.setIncludeInferred(includeInferred);        
        return  tupleQuery.evaluate();
    }
    
    public static GraphResult query(RepositoryConnection connection, GraphQueryModel graphQueryModel, boolean includeInferred) throws StoreException, MalformedQueryException{
        QUERY_HOLDER.set(graphQueryModel);
        GraphQuery graphQuery = connection.prepareGraphQuery(DirectQuery.DIRECTQUERY, "");
        graphQuery.setIncludeInferred(includeInferred);
        return graphQuery.evaluate();
    }

}
