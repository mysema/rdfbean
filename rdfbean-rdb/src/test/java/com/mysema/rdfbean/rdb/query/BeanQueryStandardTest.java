/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdb.query;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.mysema.commons.lang.Pair;
import com.mysema.query.Module;
import com.mysema.query.Projectable;
import com.mysema.query.QueryExecution;
import com.mysema.query.Target;
import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.rdfbean.domains.SimpleDomain;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({SimpleType.class, SimpleType2.class})
public class BeanQueryStandardTest extends AbstractRDBTest implements SimpleDomain {
    
    protected QSimpleType v1 = new QSimpleType("v1");
    
    protected QSimpleType v2 = new QSimpleType("v2");
    
    private SimpleType2 other;
    
    private QueryExecution standardTest = new QueryExecution(Module.SQL, Target.H2){        
        @Override
        protected Pair<Projectable, List<Expression<?>>> createQuery() {
            return Pair.of((Projectable)session.from(v1, v2), Collections.<Expression<?>>emptyList());
        }
        @Override
        protected Pair<Projectable, List<Expression<?>>> createQuery(BooleanExpression filter) {
            return Pair.of((Projectable)session.from(v1, v2).where(filter), Arrays.<Expression<?>>asList(v1, v2));
        }        
    };
    
    @Test
    public void test() throws InterruptedException{
        SimpleType simpleType = new SimpleType();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        simpleType.dateProperty = cal.getTime();
        simpleType.localizedProperty = "ABCDE";
        simpleType.directProperty = "abcde";
        simpleType.numericProperty = 2;
        session.save(simpleType);
        
        SimpleType simpleType2 = new SimpleType();
        simpleType2.dateProperty = new java.util.Date(0);
        simpleType2.localizedProperty = "ABCDEF";
        simpleType2.directProperty = "abcdef";
        simpleType2.numericProperty = 3;
        session.save(simpleType2);
        
        other = new SimpleType2();              
        session.save(other);
        
        standardTest.runBooleanTests(v1.directProperty.isNull(), v2.numericProperty.isNotNull());
        standardTest.runDateTimeTests(v1.dateProperty, v2.dateProperty, simpleType.getDateProperty());
        standardTest.runNumericCasts(v1.numericProperty, v2.numericProperty, simpleType.numericProperty);
        standardTest.runNumericTests(v1.numericProperty, v2.numericProperty, simpleType.numericProperty);
        standardTest.runStringTests(v1.directProperty, v2.directProperty, simpleType.directProperty);

        // delay the report slightly
        Thread.sleep(10);
        standardTest.report();        
    }
     

}
