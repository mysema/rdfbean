package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mysema.query.QueryModifiers;
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
        assertEquals(9, query(null));
        assertEquals(2, query(new QueryModifiers(2,0)));
        assertEquals(2, query(new QueryModifiers(2,3)));
        assertEquals(0, query(new QueryModifiers(10,9)));
        assertEquals(9, query(new QueryModifiers(20,0)));
        assertEquals(5, query(new QueryModifiers(20,4)));
    }
    
    private int query(QueryModifiers modifiers){
        BeanQuery beanQuery = newQuery().from(var2).orderBy(var2.directProperty.asc());
        if (modifiers != null){
            beanQuery.restrict(modifiers);
        }
        return beanQuery.listResults(var2).getResults().size();
    }
}
