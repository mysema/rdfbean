/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Iterator;

import org.junit.Test;

public class VarNameIteratorTest {

    @Test
    public void Next() {
        Iterator<String> it = new VarNameIterator();
        for (int i = 0; i < 100; i++) {
            System.out.println(it.next());
        }
    }

}
