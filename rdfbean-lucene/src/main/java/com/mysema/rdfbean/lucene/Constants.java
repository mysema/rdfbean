/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import org.compass.core.CompassQueryBuilder;

import com.mysema.rdfbean.model.QueryLanguage;

/**
 * Constants provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface Constants {
    
    QueryLanguage<Void,CompassQueryBuilder> COMPASSQUERY = QueryLanguage.create("CompassQueryBuilder", CompassQueryBuilder.class);
    
    QueryLanguage<Void,LuceneQuery> LUCENEQUERY = QueryLanguage.create("Lucene query", LuceneQuery.class);
    
    String ALL_FIELD_NAME = "all";
    
    String BNODE_ID_PREFIX = "!";
    
    String CONTEXT_FIELD_NAME = "context";
    
    String CONTEXT_NULL = "null";
    
    String ID_FIELD_NAME = "id";
    
    String EMBEDDED_ID_FIELD_NAME = "eid";
    
    String TEXT_FIELD_NAME = "text";
}
