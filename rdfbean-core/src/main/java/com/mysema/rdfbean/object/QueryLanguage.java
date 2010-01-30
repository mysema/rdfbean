/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;


/**
 * QueryLanguage defines support a query language
 *
 * @author tiwe
 * @version $Id$
 */
public final class QueryLanguage<D,Q> {

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