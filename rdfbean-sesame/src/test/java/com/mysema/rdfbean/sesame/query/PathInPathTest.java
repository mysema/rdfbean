/*
 * Copyright (c) 2010 Mysema Ltd.
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
 * PathInPathTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PathInPathTest extends SessionTestBase {


    @Before
    public void setUp() throws StoreException{
        session = createSession(FI, SimpleType.class, SimpleType2.class);
    }
    
    @Test
    public void pathInPath(){
        int count1 = from(var2, var).where(var2.in(var.setProperty)).list(var, var2).size();
        int count2 = from(var2, var).where(var2.in(var.setProperty)).listDistinct(var, var2).size();
        assertEquals(count1, count2);
        assertEquals(4, count1);
    }
    
}
