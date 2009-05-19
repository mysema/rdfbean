/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.parser.QueryParserRegistry;
import org.openrdf.query.parser.TupleQueryModel;

/**
 * SailQueryHolder provides integration of programmatically constructed queries into Sail sessions
 *
 * @author tiwe
 * @version $Id$
 */
public class SesameQueryHolder {
    
    public static final QueryLanguage QUERYDSL = new QueryLanguage("QUERYDSL");    
    
    private static ThreadLocal<TupleQueryModel> QUERY_HOLDER = new ThreadLocal<TupleQueryModel>();    
    
    private static final QueryParser QUERYDSL_PARSER = new QueryParser(){
        @Override
        /**
         * Returns the threadbound query, ignores the parameters of the method invocation
         */
        public TupleQueryModel parseQuery(String queryStr, String baseURI)throws MalformedQueryException {
            return QUERY_HOLDER.get();
        }        
    };
    
    /**
     * Initialzie the SailQueryHolder
     * 
     */
    public static void init(){
        QueryParserRegistry.getInstance().add(new QueryParserFactory(){
            @Override
            public QueryParser getParser() {
                return QUERYDSL_PARSER;
            }
            @Override
            public QueryLanguage getQueryLanguage() {
                return QUERYDSL;
            }            
        });
    }

    /**
     * Set the threadbound query
     * 
     * @param query
     */
    public static void set(TupleQueryModel query) {
        QUERY_HOLDER.set(query);        
    }

}
