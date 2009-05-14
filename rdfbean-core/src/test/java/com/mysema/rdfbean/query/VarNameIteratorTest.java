/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Iterator;

import org.junit.Test;

import com.mysema.rdfbean.query.VarNameIterator;

/**
 * VarNameIteratorTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class VarNameIteratorTest {

    @Test
    public void testNext() {
        Iterator<String> it = new VarNameIterator();
        for (int i = 0; i < 100; i++){
            System.out.println(it.next());
        }
    }

}
