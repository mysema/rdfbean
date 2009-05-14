/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * StringHandlingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class StringHandlingTest extends AbstractSesameQueryTest{

    @Test
    public void startsWith(){
        assertEquals(1, where(var.listProperty(1).directProperty.startsWith("ns")).list(var).size());     
    }
    
    @Test
    public void startsWithIgnoreCase(){
        assertEquals(1, where(var.listProperty(1).directProperty.startsWith("NS",false)).list(var).size());   
    }
    
    @Test
    public void endsWith(){
        assertEquals(1, where(var.listProperty(1).directProperty.endsWith("ix")).list(var).size());     
    }
    
    @Test
    public void endsWithIgnoreCase(){
        assertEquals(1, where(var.listProperty(1).directProperty.endsWith("IX",false)).list(var).size());   
    }
}
