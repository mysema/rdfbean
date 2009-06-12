/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;


/**
 * OrderTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OrderTest extends AbstractSesameQueryTest{
    
    @Test
    public void simpleOrder(){
        List<SimpleType> asc = newQuery().from(var).orderBy(var.directProperty.asc()).list(var);
        System.out.println();
        
        List<SimpleType> desc = newQuery().from(var).orderBy(var.directProperty.desc()).list(var);
        
        if (asc.equals(desc)){
            System.out.println("asc "+  asc);
            System.out.println("desc " + desc);
        }
        assertFalse(asc.equals(desc));
    }

}
