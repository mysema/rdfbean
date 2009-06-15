/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.SimpleBeanQuery;

/**
 * PagingReferenceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PagingReferenceTest extends PagingTest{
    protected BeanQuery newQuery(){
        return new SimpleBeanQuery(session);
    }  
}
