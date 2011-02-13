/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.MapPath;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({SimpleType.class, SimpleType2.class})
public class MapQueriesTest extends SessionTestBase{

    protected QSimpleType v1 = new QSimpleType("v1");

    protected QSimpleType v2 = new QSimpleType("v2");

    private SimpleType2 instance;

    @Before
    public void setUp(){
        instance = session.from(QSimpleType2.simpleType2).uniqueResult(QSimpleType2.simpleType2);
    }

    @Test
    public void MapFilters() {
        for (BooleanExpression f : mapFilters(v1.mapProperty, v2.mapProperty, "", instance)){
            System.err.println("\n" + f);
            session.from(v1,v2).where(f).list(v1.directProperty);
        }
    }

    @Test
    public void MapProjections() {
        for (Expression<?> pr : mapProjections(v1.mapProperty, v2.mapProperty, "", instance)){
            System.err.println("\n" + pr);
            session.from(v1,v2).list(pr);
        }
    }

    private static <K,V> Collection<BooleanExpression> mapFilters(MapPath<K,V,?> expr, MapPath<K,V,?> other, K knownKey, V knownValue) {
        return Arrays.<BooleanExpression>asList(
          expr.isEmpty(),
          expr.isNotEmpty(),
          expr.containsKey(knownKey),
          expr.containsValue(knownValue),
          expr.get(knownKey).eq(knownValue)
        );
    }

    private static <K,V> Collection<Expression<?>> mapProjections(MapPath<K,V,?> expr, MapPath<K,V,?> other, K knownKey, V knownValue) {
        return Arrays.<Expression<?>>asList(
          expr.get(knownKey)
        );
    }
}
