/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.query.QueryMetadata;
import com.mysema.rdfbean.object.BeanQuery;


/**
 * QueryLanguage defines support for a query language
 *
 * @author tiwe
 * @version $Id$
 */
public final class QueryLanguage<D,Q> {

    public static final QueryLanguage<String, SPARQLQuery> SPARQL = create("SPARQL", String.class, SPARQLQuery.class);
   
    public static final QueryLanguage<QueryMetadata, BooleanQuery> BOOLEAN = create("BOOLEAN", QueryMetadata.class, BooleanQuery.class);
    
    public static final QueryLanguage<QueryMetadata, GraphQuery> GRAPH = create("GRAPH", QueryMetadata.class, GraphQuery.class);
    
    public static final QueryLanguage<QueryMetadata, TupleQuery> TUPLE = create("TUPLE", QueryMetadata.class, TupleQuery.class);
    
    public static final QueryLanguage<Void,BeanQuery> QUERYDSL = create("Querydsl", BeanQuery.class);
       
    public static <Q> QueryLanguage<Void,Q> create(String name, Class<Q> queryType){
        return new QueryLanguage<Void,Q>(name, Void.class, queryType);
    }
    
    public static <D,Q> QueryLanguage<D,Q> create(String name, Class<D> defType, Class<Q> queryType){
        return new QueryLanguage<D,Q>(name, defType, queryType);
    }
    
    private final String name;
    
    private final Class<D> definitionType;
    
    private final Class<Q> queryType;
    
    private QueryLanguage(String name, Class<D> definitionType, Class<Q> queryType){
        this.name = name;
        this.definitionType = definitionType;
        this.queryType = queryType;
    }

    public String getName() {
        return name;
    }

    public Class<D> getDefinitionType() {
        return definitionType;
    }

    public Class<Q> getQueryType() {
        return queryType;
    }

}
