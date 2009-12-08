/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * QueryBuilder provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BooleanQueryBuilder {
    
    private final BooleanQuery booleanQuery = new BooleanQuery();
    
    public BooleanQueryBuilder and(Term term){
        return and(new TermQuery(term));
    }
    
    public BooleanQueryBuilder and(Query qry){
        booleanQuery.add(qry, BooleanClause.Occur.MUST);
        return this;
    }
    
    public BooleanQuery getQuery(){
        return booleanQuery;
    }

}
