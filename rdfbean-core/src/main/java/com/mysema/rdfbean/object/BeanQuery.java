/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import com.mysema.query.Projectable;
import com.mysema.query.Query;
import com.mysema.query.types.path.PEntity;

/**
 * BeanQuery is a Query interface for Java Bean based RDF data projections
 *
 * @author tiwe
 * @version $Id$
 */
public interface BeanQuery extends Query<BeanQuery>, Projectable {
    
    /**
     * Defines the sources of the query
     * 
     * @param o
     * @return
     */
    BeanQuery from(PEntity<?>... o);
       
}