/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * CountTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CountTest extends SessionTestBase{


    @Before
    public void setUp() throws StoreException{
        session = createSession(FI, SimpleType.class, SimpleType2.class);
    }
    
    @Test
    public void test(){
        assertTrue(from(var).count() > 0);        
    }
    
    @Test
    @Ignore
    public void countWithLimitAndOffset(){
        // NOTE : invalid test
        assertTrue(from(var).limit(0l).offset(0).count() > 0);
    }
}
