/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections15.ComparatorUtils;
import org.junit.Test;

import com.mysema.query.grammar.Ops;
import com.mysema.query.grammar.Ops.Op;


/**
 * SailOpsTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SailOpsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testOps() throws IllegalArgumentException, IllegalAccessException{
        SesameOps sailOps = SesameOps.DEFAULT;
        Set<Field> opFields = new TreeSet<Field>(ComparatorUtils.chainedComparator(
                new BeanComparator("declaringClass.simpleName"),
                new BeanComparator("name")));
        for (Class<?> cl : Arrays.<Class<?>>asList(Ops.class, 
                Ops.OpDateTime.class, 
                Ops.OpMath.class, 
                Ops.OpNumberAgg.class, 
                Ops.OpString.class)){
            for (Field field : cl.getDeclaredFields()){
                if (Op.class.isAssignableFrom(field.getType())){
                    opFields.add(field);
                }
            }            
        }
        
        int counter = 0;
        for (Field field : opFields){
            if (sailOps.getTransformer((Op<?>) field.get(null)) == null){
                System.err.println(
                        field.getDeclaringClass().getSimpleName()+"."+
                        field.getName() + 
                        " missing in sailOps");
                counter++;
            }
        }
        if (counter > 0){
            System.err.println(counter + " of " + opFields.size() + " missing");
        }
    }
}
