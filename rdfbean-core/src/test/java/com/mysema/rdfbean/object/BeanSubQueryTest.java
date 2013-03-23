/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;

public class BeanSubQueryTest {

    private BeanSubQuery beanSubQuery = new BeanSubQuery();

    private PathBuilder<Object> pathBuilder = new PathBuilder<Object>(Object.class, "obj");

    private StringPath stringPath = pathBuilder.getString("str");

    @Before
    public void setUp() {
        beanSubQuery.from(pathBuilder);
    }

    @Test
    public void Count() {
        beanSubQuery.count();
    }

    @Test
    public void Exists() {
        beanSubQuery.exists();
    }

    @Test
    public void ListExprOfQExprOfQExprOfQArray() {
        beanSubQuery.list(pathBuilder);
    }

    @Test
    public void NotExists() {
        beanSubQuery.notExists();
    }

    @Test
    public void OrderBy() {
        beanSubQuery.orderBy(stringPath.asc());
    }

    @Test
    public void UniqueEBoolean() {
        beanSubQuery.unique(stringPath);
    }

    @Test
    public void Where() {
        beanSubQuery.where(stringPath.isNotNull());
    }

}
