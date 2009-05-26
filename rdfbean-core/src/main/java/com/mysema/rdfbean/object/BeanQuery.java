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
    
    BeanQuery from(PEntity<?>... o);
    
    BeanQuery orderBy(OrderSpecifier<?>... o);
    
    BeanQuery where(EBoolean... o);
    
    BeanQuery limit(long limit);
    
    BeanQuery offset(long offset);
    
    BeanQuery restrict(QueryModifiers mod);

}