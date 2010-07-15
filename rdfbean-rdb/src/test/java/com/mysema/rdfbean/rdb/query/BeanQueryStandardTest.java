/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.rdb.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.Pair;
import com.mysema.query.Module;
import com.mysema.query.Projectable;
import com.mysema.query.QueryExecution;
import com.mysema.query.Target;
import com.mysema.query.types.Expr;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.rdfbean.domains.SimpleDomain;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

/**
 * BeanQueryStandardTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@Ignore
@SessionConfig({SimpleType.class, SimpleType2.class})
public class BeanQueryStandardTest extends AbstractRDBTest implements SimpleDomain {
    
    protected QSimpleType v1 = new QSimpleType("v1");
    
    protected QSimpleType v2 = new QSimpleType("v2");
    
    private SimpleType2 other;
    
    private QueryExecution standardTest = new QueryExecution(Module.RDFBEAN, Target.MEM){        
        @Override
        protected Pair<Projectable, List<Expr<?>>> createQuery() {
            return Pair.of((Projectable)session.from(v1, v2), Collections.<Expr<?>>emptyList());
        }
        @Override
        protected Pair<Projectable, List<Expr<?>>> createQuery(EBoolean filter) {
            return Pair.of((Projectable)session.from(v1, v2).where(filter), Arrays.<Expr<?>>asList(v1, v2));
        }        
    };
    
    @Test
    public void test() throws InterruptedException{
        SimpleType simpleType = new SimpleType();
        simpleType.dateProperty = new java.util.Date();
        simpleType.localizedProperty = "str";
        simpleType.directProperty = "XXX";
        simpleType.numericProperty = 2;
        session.save(simpleType);
        
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
