/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;

import com.mysema.query.Projectable;
import com.mysema.query.QueryModifiers;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;

/**
 * BeanQuery is a Query interface for Java Bean based RDF data projections
 *
 * @author tiwe
 * @version $Id$
 */
public interface BeanQuery extends Projectable, Closeable{
    
    /**
     * Defines the sources of the query
     * 
     * @param o
     * @return
     */
    BeanQuery from(PEntity<?>... o);
    
    /**
     * Defines the order of the query results
     * 
     * @param o
     * @return
     */
    BeanQuery orderBy(OrderSpecifier<?>... o);
    
    /**
     * Defines the filters of the query
     * 
     * @param o multiple mandatory fileters
     * @return
     */
    BeanQuery where(EBoolean... o);
    
    /**
     * Maximum number of results
     * 
     * @param limit
     * @return
     */
    BeanQuery limit(long limit);
    
    /**
     * Offset of results
     * 
     * @param offset
     * @return
     */
    BeanQuery offset(long offset);
    
    /**
     * Defines limit and offset
     * 
     * @param mod
     * @return
     */
    BeanQuery restrict(QueryModifiers mod);

}