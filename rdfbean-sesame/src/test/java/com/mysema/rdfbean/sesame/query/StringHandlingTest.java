/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.query.types.path.PString;


/**
 * StringHandlingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class StringHandlingTest extends AbstractSesameQueryTest{
    
    private PString stringPath = var.listProperty(1).directProperty;

    @Test
    public void startsWith(){
        assertEquals(1, where(stringPath.startsWith("ns")).list(var).size());     
    }
    
    @Test
    public void startsWithIgnoreCase(){
        assertEquals(1, where(stringPath.startsWith("NS",false)).list(var).size());   
    }
    
    @Test
    public void endsWith(){
        assertEquals(1, where(stringPath.endsWith("ix")).list(var).size());     
    }
    
    @Test
    public void endsWithIgnoreCase(){
        assertEquals(1, where(stringPath.endsWith("IX",false)).list(var).size());   
    }
    
    
}
