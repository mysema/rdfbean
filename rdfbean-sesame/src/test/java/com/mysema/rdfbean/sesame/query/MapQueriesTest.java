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
import org.openrdf.store.StoreException;

import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.EMap;
import com.mysema.query.types.expr.Expr;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * MapQueriesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MapQueriesTest extends SessionTestBase{
    
    protected QSimpleType v1 = new QSimpleType("v1");
    
    protected QSimpleType v2 = new QSimpleType("v2");

    private SimpleType2 instance;
    

    @Before
    public void setUp() throws StoreException{
        session = createSession(FI, SimpleType.class, SimpleType2.class);
        instance = from(QSimpleType2.simpleType2).uniqueResult(QSimpleType2.simpleType2);    
    }
    
    @Test
    public void mapFilters() {
        for (EBoolean f : mapFilters(v1.mapProperty, v2.mapProperty, "", instance)){
            System.err.println("\n" + f);
            from(v1,v2).where(f).list(v1.directProperty);
        }             
    }

    @Test
    public void mapProjections() {
        for (Expr<?> pr : mapProjections(v1.mapProperty, v2.mapProperty, "", instance)){
            System.err.println("\n" + pr);
            from(v1,v2).list(pr);
        }    
    }
    
    private static <K,V> Collection<EBoolean> mapFilters(EMap<K,V> expr, EMap<K,V> other, K knownKey, V knownValue) {
        return Arrays.<EBoolean>asList(
          expr.isEmpty(),
          expr.isNotEmpty(),
          expr.containsKey(knownKey),
          expr.containsValue(knownValue),          
          expr.get(knownKey).eq(knownValue)
        );
    }
        
    private static <K,V> Collection<Expr<?>> mapProjections(EMap<K,V> expr, EMap<K,V> other, K knownKey, V knownValue) {
        return Arrays.<Expr<?>>asList(
          expr.get(knownKey)
        );
    }
}
