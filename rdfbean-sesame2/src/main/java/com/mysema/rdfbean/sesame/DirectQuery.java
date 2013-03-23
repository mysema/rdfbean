/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.parser.QueryParserRegistry;
import org.openrdf.repository.RepositoryConnection;

import com.mysema.rdfbean.model.RepositoryException;

/**
 * @author tiwe
 * 
 */
public final class DirectQuery {

    private static final ThreadLocal<ParsedQuery> QUERY_HOLDER = new ThreadLocal<ParsedQuery>();

    private static final QueryLanguage DIRECTQUERY = new QueryLanguage("DIRECTQUERY");

    private static final QueryParser DIRECTQUERY_PARSER = new QueryParser() {
        @Override
        public ParsedQuery parseQuery(String queryStr, String baseURI) {
            return QUERY_HOLDER.get();
        }

        @Override
        public ParsedUpdate parseUpdate(String arg0, String arg1)
                throws MalformedQueryException {
            throw new UnsupportedOperationException();
        }
    };

    static {
        QueryParserRegistry.getInstance().add(new QueryParserFactory() {
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

    public static TupleQuery getQuery(RepositoryConnection connection, ParsedTupleQuery tupleQueryModel,
            boolean includeInferred) {
        try {
            // System.err.println(tupleQueryModel.getTupleExpr());
            QUERY_HOLDER.set(tupleQueryModel);
            TupleQuery tupleQuery = connection.prepareTupleQuery(DirectQuery.DIRECTQUERY, "");
            tupleQuery.setIncludeInferred(includeInferred);
            return tupleQuery;
        } catch (MalformedQueryException e) {
            throw new RepositoryException(e);
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }

    }

    public static GraphQuery getQuery(RepositoryConnection connection, ParsedGraphQuery graphQueryModel,
            boolean includeInferred) {
        try {
            QUERY_HOLDER.set(graphQueryModel);
            GraphQuery graphQuery = connection.prepareGraphQuery(DirectQuery.DIRECTQUERY, "");
            graphQuery.setIncludeInferred(includeInferred);
            return graphQuery;
        } catch (MalformedQueryException e) {
            throw new RepositoryException(e);
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    public static BooleanQuery getQuery(RepositoryConnection connection, ParsedBooleanQuery booleanQueryModel,
            boolean includeInferred) {
        try {
            QUERY_HOLDER.set(booleanQueryModel);
            BooleanQuery booleanQuery = connection.prepareBooleanQuery(DirectQuery.DIRECTQUERY, "");
            booleanQuery.setIncludeInferred(includeInferred);
            return booleanQuery;
        } catch (MalformedQueryException e) {
            throw new RepositoryException(e);
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    public static TupleQueryResult query(RepositoryConnection connection, ParsedTupleQuery tupleQueryModel,
            boolean includeInferred) throws org.openrdf.repository.RepositoryException, QueryEvaluationException {
        return getQuery(connection, tupleQueryModel, includeInferred).evaluate();
    }

    public static GraphQueryResult query(RepositoryConnection connection, ParsedGraphQuery graphQueryModel,
            boolean includeInferred) throws org.openrdf.repository.RepositoryException, QueryEvaluationException {
        return getQuery(connection, graphQueryModel, includeInferred).evaluate();
    }

    public static boolean query(RepositoryConnection connection, ParsedBooleanQuery booleanQueryModel,
            boolean includeInferred) throws org.openrdf.repository.RepositoryException, QueryEvaluationException {
        return getQuery(connection, booleanQueryModel, includeInferred).evaluate();
    }

    private DirectQuery() {
    }

}
