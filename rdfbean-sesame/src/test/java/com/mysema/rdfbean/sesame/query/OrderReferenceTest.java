/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.SimpleBeanQuery;

/**
 * OrderReference provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OrderReferenceTest extends OrderTest{
    protected BeanQuery newQuery(){
        return new SimpleBeanQuery(session);
    }
}