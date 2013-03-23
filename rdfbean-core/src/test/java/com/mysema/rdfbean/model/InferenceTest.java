/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import org.junit.Test;

public class InferenceTest {

    @Test
    public void test() {
        for (InferenceOptions inf : InferenceOptions.values()) {
            System.out.println(inf);
        }
    }

}
