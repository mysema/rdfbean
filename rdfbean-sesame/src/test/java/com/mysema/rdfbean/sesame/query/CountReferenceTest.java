/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.SimpleBeanQuery;

/**
 * CountReferenceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CountReferenceTest extends CountTest{
    protected BeanQuery newQuery(){
        return new SimpleBeanQuery(session);
    } 
}
