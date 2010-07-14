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
    
    private String knownStringValue = "propertymap";
    
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
        SimpleType st = session.from(v1).uniqueResult(v1);
        SimpleType2 inMap = st.getMapProperty().values().iterator().next();
        SimpleType2 inList = st.getListProperty().iterator().next();
        SimpleType2 inSet = st.getSetProperty().iterator().next();
        other = new SimpleType2();        
        session.save(other);
        
        standardTest.runBooleanTests(v1.directProperty.isNull(), v2.numericProperty.isNotNull());
        standardTest.runCollectionTests(v1.setProperty, v2.setProperty, inSet, other);
//        standardTest.dateTests(v1.dateProperty, v2.dateProperty, st.getDateProperty());
        standardTest.runDateTimeTests(v1.dateProperty, v2.dateProperty, st.getDateProperty());
        standardTest.runListTests(v1.listProperty, v2.listProperty, inList, other);
        standardTest.runMapTests(v1.mapProperty, v2.mapProperty, "target_idspace", inMap, "xxx", other);
        standardTest.runNumericCasts(v1.numericProperty, v2.numericProperty, 1);
        standardTest.runNumericTests(v1.numericProperty, v2.numericProperty, 10);
        standardTest.runStringTests(v1.directProperty, v2.directProperty, knownStringValue);
//        standardTest.timeTests(null, null, null);
        
        // delay the report slightly
        Thread.sleep(10);
        standardTest.report();        
    }
     

}