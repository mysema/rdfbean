/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.parser.QueryParserRegistry;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

/**
 * DirectQuery provides integration of programmatically constructed queries into Repository connection
 *
 * @author tiwe
 * @version $Id$
 */
public final class DirectQuery {

    private DirectQuery(){}

    private static final ThreadLocal<ParsedQuery> QUERY_HOLDER = new ThreadLocal<ParsedQuery>();

    private static final QueryLanguage DIRECTQUERY = new QueryLanguage("DIRECTQUERY");

    private static final QueryParser DIRECTQUERY_PARSER = new QueryParser(){
        @Override
        /**
         * Returns the thread bound query, ignores the parameters of the method invocation
         */
        public ParsedQuery parseQuery(String queryStr, String baseURI) {
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

    public static TupleQueryResult query(RepositoryConnection connection,
            ParsedTupleQuery tupleQueryModel,
            boolean includeInferred) throws SailException, RepositoryException, MalformedQueryException, QueryEvaluationException{
        QUERY_HOLDER.set(tupleQueryModel);
        TupleQuery tupleQuery = connection.prepareTupleQuery(DirectQuery.DIRECTQUERY, "");
        tupleQuery.setIncludeInferred(includeInferred);
        return  tupleQuery.evaluate();
    }

    public static GraphQueryResult query(RepositoryConnection connection,
            ParsedGraphQuery graphQueryModel,
            boolean includeInferred) throws SailException, RepositoryException, MalformedQueryException, QueryEvaluationException{
        QUERY_HOLDER.set(graphQueryModel);
        GraphQuery graphQuery = connection.prepareGraphQuery(DirectQuery.DIRECTQUERY, "");
        graphQuery.setIncludeInferred(includeInferred);
        return graphQuery.evaluate();
    }

}
