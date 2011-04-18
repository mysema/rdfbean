/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.query.QueryMetadata;


/**
 * QueryLanguage defines support for a query language
 *
 * @author tiwe
 */
public final class QueryLanguage<D,Q> {

    public static final QueryLanguage<String, SPARQLQuery> SPARQL = new QueryLanguage<String, SPARQLQuery>("SPARQL");
   
    public static final QueryLanguage<QueryMetadata, BooleanQuery> BOOLEAN = new QueryLanguage<QueryMetadata, BooleanQuery>("BOOLEAN");
    
    public static final QueryLanguage<QueryMetadata, GraphQuery> GRAPH = new QueryLanguage<QueryMetadata, GraphQuery>("GRAPH");
    
    public static final QueryLanguage<QueryMetadata, TupleQuery> TUPLE = new QueryLanguage<QueryMetadata, TupleQuery>("TUPLE");
    
    private final String name;
    
    private QueryLanguage(String name){
        this.name = name;
    }
    @Override
    public String toString(){
        return name;
    }

}
