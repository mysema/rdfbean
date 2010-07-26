/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.types.path.PString;
import com.mysema.query.types.path.PathBuilder;

/**
 * BeanSubQueryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanSubQueryTest {
    
    private BeanSubQuery beanSubQuery = new BeanSubQuery();

    private PathBuilder<Object> pathBuilder = new PathBuilder<Object>(Object.class, "obj");
    
    private PString stringPath = pathBuilder.getString("str");
    
    @Before
    public void setUp(){
        beanSubQuery.from(pathBuilder);
    }
    
    @Test
    public void testCount() {
        beanSubQuery.count();
    }

    @Test
    public void testExists() {
        beanSubQuery.exists();
    }

    @Test
    public void testListExprOfQExprOfQExprOfQArray() {
        beanSubQuery.list(pathBuilder);
    }

    @Test
    public void testNotExists() {
        beanSubQuery.notExists();
    }

    @Test
    public void testOrderBy() {
        beanSubQuery.orderBy(stringPath.asc());
    }

    @Test
    public void testUniqueEBoolean() {
        beanSubQuery.unique(stringPath);
    }

    @Test
    public void testWhere() {
        beanSubQuery.where(stringPath.isNotNull());
    }

}
