/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

/**
 * CountTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CountTest extends AbstractSesameQueryTest{

    @Test
    public void test(){
        assertTrue(newQuery().from(var).count() > 0);        
    }
    
    @Test
    @Ignore
    public void countWithLimitAndOffset(){
        // NOTE : invalid test
        assertTrue(newQuery().from(var).limit(0l).offset(0).count() > 0);
    }
}
