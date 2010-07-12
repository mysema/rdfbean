/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.TestConfig;

/**
 * DateTimeTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@TestConfig({SimpleType.class, SimpleType2.class})
public class DateTimeTest extends SessionTestBase{
    
    private Session session;
    
    @Test
    public void test(){
        QSimpleType v1 = new QSimpleType("v1");
        SimpleType st = session.from(v1).uniqueResult(v1);
        System.out.println(st.getDateProperty());
        assertTrue(session.from(v1).where(v1.dateProperty.eq(st.getDateProperty())).count() > 0);
    }

}
