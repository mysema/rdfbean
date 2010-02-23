/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.Module;
import com.mysema.query.StandardTest;
import com.mysema.query.Target;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.Expr;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * BeanQueryStandardTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanQueryStandardTest extends SessionTestBase {
    
    private String knownStringValue = "propertymap";
    
    protected QSimpleType v1 = new QSimpleType("v1");
    
    protected QSimpleType v2 = new QSimpleType("v2");
    
    private SimpleType2 other;
    

    @Before
    public void setUp() throws StoreException{
        session = createSession(FI, SimpleType.class, SimpleType2.class);
    }
    
    private StandardTest standardTest = new StandardTest(Module.RDFBEAN, Target.MEM){
        @Override
        public int executeFilter(EBoolean f) {
            if (f.toString().equals("size(v1.mapProperty) > 0") || // map size is not supported
                f.toString().matches("com.*.SimpleType2@.* in v1.listProperty")){ // searching for items sequence is not supported
                return 1;    
            }else{                
                return from(v1, v2).where(f).list(v1, v2).size();
            }
            
        }
        @Override
        public int executeProjection(Expr<?> pr) {
            return from(v1, v2).list(v1, v2).size();
        }        
    };
    
    @Test
    public void test() throws InterruptedException{
        SimpleType st = from(v1).uniqueResult(v1);
        SimpleType2 inMap = st.getMapProperty().values().iterator().next();
        SimpleType2 inList = st.getListProperty().iterator().next();
        SimpleType2 inSet = st.getSetProperty().iterator().next();
        other = new SimpleType2();        
        session.save(other);
        
        standardTest.booleanTests(v1.directProperty.isNull(), v2.numericProperty.isNotNull());
        standardTest.collectionTests(v1.setProperty, v2.setProperty, inSet, other);
//        standardTest.dateTests(v1.dateProperty, v2.dateProperty, st.getDateProperty());
        standardTest.dateTimeTests(v1.dateProperty, v2.dateProperty, st.getDateProperty());
        standardTest.listTests(v1.listProperty, v2.listProperty, inList, other);
        standardTest.mapTests(v1.mapProperty, v2.mapProperty, "target_idspace", inMap, "xxx", other);
        standardTest.numericCasts(v1.numericProperty, v2.numericProperty, 1);
        standardTest.numericTests(v1.numericProperty, v2.numericProperty, 10);
        standardTest.stringTests(v1.directProperty, v2.directProperty, knownStringValue);
//        standardTest.timeTests(null, null, null);
        
        // delay the report slightly
        Thread.sleep(10);
        standardTest.report();        
    }
        
    @Override
    public void tearDown() throws IOException{
        if (other != null) session.delete(other);
        super.tearDown();
    }

}
