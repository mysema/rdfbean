/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * PathInPathTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PathInPathTest extends AbstractSesameQueryTest {

    @Test
    public void pathInPath(){
        int count1 = newQuery().from(var2, var).where(var2.in(var.setProperty)).list(var, var2).size();
        int count2 = newQuery().from(var2, var).where(var2.in(var.setProperty)).listDistinct(var, var2).size();
        assertEquals(count1, count2);
        assertEquals(4, count1);
    }
    
}
