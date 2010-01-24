/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * DuplicateVarNamesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DuplicateVarNamesTest extends SessionTestBase{


    @Before
    public void setUp() throws StoreException{
        session = createSession(FI, SimpleType.class, SimpleType2.class);
    }
    
    @Test
    public void test(){
        QSimpleType v = new QSimpleType("va");
        assertEquals(2, from(v).list(v.directProperty).size());
    }
}
