/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Test;

public class TransactionalMethodMatcherTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void test(){
        TransactionalMethodMatcher matcher = new TransactionalMethodMatcher();
        for (Class<?> cl : Arrays.asList(ServiceA.class, ServiceB.class, ServiceBImpl.class, ServiceC.class)){
            System.out.println(cl.getSimpleName());
            for (Method m : cl.getMethods()){
                if (m.getName().toLowerCase().contains("tx")){
                    boolean matched = matcher.matches(m);
                    System.out.println(" " + m.getName() + " : " + matched);    
                    assertEquals(m.getName()+"failed", matched, m.getName().toLowerCase().startsWith("tx")); 
                }                
            }
            System.out.println();
        }
    }

}
