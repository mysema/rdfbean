/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import javax.annotation.Nullable;

import org.apache.commons.collections15.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.QueryModifiers;
import com.mysema.query.SearchResults;
import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * PagingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PagingTest extends SessionTestBase implements EntityDomain {

    private static final Entity entity = Alias.alias(Entity.class);
    
    @Before
    public void setUp() throws StoreException{
        session = createSession(FI, Entity.class);
        for (int i = 0; i < 9; i++){
            Entity entity = new Entity();
            entity.property = String.valueOf(i);
            session.save(entity);
        }
        session.clear();
    }
    
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
    
    private void assertResultSize(int total, int size, @Nullable QueryModifiers modifiers){        
        // via list
        assertEquals(size, createQuery(modifiers).list($(entity)).size());
        System.out.println();
        
        // via results
        SearchResults<?> results = createQuery(modifiers).listResults($(entity));
        assertEquals(total, results.getTotal());
        assertEquals(size, results.getResults().size());
        System.out.println();        
        
        // via iterator
        assertEquals(size, IteratorUtils.toList(createQuery(modifiers).iterate($(entity))).size());
        System.out.println();
    }
    
    private BeanQuery createQuery(@Nullable QueryModifiers modifiers){
        BeanQuery beanQuery = from($(entity)).orderBy($(entity.getProperty()).asc());
        if (modifiers != null) beanQuery.restrict(modifiers);
        return beanQuery;
    }
}
