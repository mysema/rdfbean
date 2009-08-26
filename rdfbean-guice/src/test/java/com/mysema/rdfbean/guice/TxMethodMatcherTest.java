package com.mysema.rdfbean.guice;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Test;


/**
 * TxMethodMatcherTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class TxMethodMatcherTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void test(){
        TxMethodMatcher matcher = new TxMethodMatcher();
        for (Class<?> cl : Arrays.asList(ServiceA.class, ServiceB.class, ServiceBImpl.class)){
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
