/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({SimpleType.class, SimpleType2.class})
public class PathInPathTest extends SessionTestBase {
    
    @Test
    public void PathInPath(){
        int count1 = session.from(var2, var).where(var2.in(var.setProperty)).list(var, var2).size();
        int count2 = session.from(var2, var).where(var2.in(var.setProperty)).listDistinct(var, var2).size();
        assertEquals(count1, count2);
        assertEquals(4, count1);
    }
    
}
