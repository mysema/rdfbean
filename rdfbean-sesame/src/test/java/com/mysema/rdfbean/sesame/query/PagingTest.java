/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;

import com.mysema.query.QueryModifiers;
import com.mysema.query.SearchResults;
import com.mysema.rdfbean.object.BeanQuery;

/**
 * PagingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PagingTest extends AbstractSesameQueryTest{

    @Test
    public void test(){
        assertResultSize(9, 9, null);
        assertResultSize(9, 2, new QueryModifiers(2l,null));
        assertResultSize(9, 2, new QueryModifiers(2l,0l));
        assertResultSize(9, 2, new QueryModifiers(2l,3l));        
        assertResultSize(9, 9, new QueryModifiers(20l,null));
        assertResultSize(9, 9, new QueryModifiers(20l,0l));
        assertResultSize(9, 5, new QueryModifiers(20l,4l));
        assertResultSize(9, 0, new QueryModifiers(10l,9l));
    }
    
    private void assertResultSize(int total, int size, QueryModifiers modifiers){        
        // via list
        assertEquals(size, createQuery(modifiers).list(var2).size());
        
        // via results
        SearchResults<?> results = createQuery(modifiers).listResults(var2);
        assertEquals(total, results.getTotal());
        assertEquals(size, results.getResults().size());
                
        // via count (ignore limit and offset)
        assertEquals(total, createQuery(modifiers).count());
        
        // via iterator
        assertEquals(size, IteratorUtils.toList(createQuery(modifiers).iterate(var2)).size());
    }
    
    private BeanQuery createQuery(QueryModifiers modifiers){
        BeanQuery beanQuery = newQuery().from(var2);
        if (modifiers != null) beanQuery.restrict(modifiers);
        return beanQuery;
    }
}
