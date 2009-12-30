/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * DateTimeTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DateTimeTest extends AbstractSesameQueryTest{
    
    @Test
    public void test(){
        QSimpleType v1 = new QSimpleType("v1");
        SimpleType st = newQuery().from(v1).uniqueResult(v1);
        System.out.println(st.getDateProperty());
        assertTrue(newQuery().from(v1).where(v1.dateProperty.eq(st.getDateProperty())).count() > 0);
    }

}
