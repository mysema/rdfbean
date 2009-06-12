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

import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.rdfbean.object.ConverterRegistry;


/**
 * SailOpsTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SesameOpsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testOps() throws IllegalArgumentException, IllegalAccessException{
        SesameOps sesameOps = new SesameOps();
        Set<Field> opFields = new TreeSet<Field>(ComparatorUtils.chainedComparator(
                new BeanComparator("declaringClass.simpleName"),
                new BeanComparator("name")));
        for (Class<?> cl : Arrays.<Class<?>>asList(Ops.class, 
                Ops.DateTimeOps.class, 
                Ops.MathOps.class,                 
                Ops.StringOps.class)){
            for (Field field : cl.getDeclaredFields()){
                if (Operator.class.isAssignableFrom(field.getType())){
                    opFields.add(field);
                }
            }            
        }
        
        int counter = 0;
        for (Field field : opFields){
            if (sesameOps.getTransformer((Operator<?>) field.get(null)) == null){
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
