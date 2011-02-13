/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({SimpleType.class, SimpleType2.class})
public class CountTest extends SessionTestBase{
        
    @Test
    public void test(){
        assertTrue(session.from(var).count() > 0);        
    }
    
    @Test
    @Ignore
    public void countWithLimitAndOffset(){
        // NOTE : invalid test
        assertTrue(session.from(var).limit(0l).offset(0).count() > 0);
    }
}
