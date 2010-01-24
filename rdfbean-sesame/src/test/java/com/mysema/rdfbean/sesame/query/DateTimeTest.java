/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * DateTimeTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DateTimeTest extends SessionTestBase{
    

    @Before
    public void setUp() throws StoreException{
        session = createSession(FI, SimpleType.class, SimpleType2.class);
    }
    
    @Test
    public void test(){
        QSimpleType v1 = new QSimpleType("v1");
        SimpleType st = from(v1).uniqueResult(v1);
        System.out.println(st.getDateProperty());
        assertTrue(from(v1).where(v1.dateProperty.eq(st.getDateProperty())).count() > 0);
    }

}
