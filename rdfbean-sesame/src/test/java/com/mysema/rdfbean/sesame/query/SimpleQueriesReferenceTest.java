/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import org.junit.Test;

import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.SimpleBeanQuery;

/**
 * SimpleQueriesReference provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SimpleQueriesReferenceTest extends SimpleQueriesTest{        
    protected BeanQuery newQuery(){
        return new SimpleBeanQuery(session);
    }        
    @Test
    public void listAccess(){
        // FIXME
    }        
    @Test
    public void listAccess2(){
        // FIXME
    }        
    @Test
    public void localizedMap(){
        // FIXME
    }        
    @Test
    public void localizedMap2(){
        // FIXME
    }
}