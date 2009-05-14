/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;

import com.mysema.query.Projectable;
import com.mysema.query.grammar.OrderSpecifier;
import com.mysema.query.grammar.types.Expr;

/**
 * BeanQuery is a Query interface for Java Bean based RDF data projections
 *
 * @author tiwe
 * @version $Id$
 */
public interface BeanQuery extends Projectable, Closeable{
    
    BeanQuery from(Expr<?>... o);
    
    BeanQuery orderBy(OrderSpecifier<?>... o);
    
    BeanQuery where(Expr.EBoolean... o);
    
//    BeanQuery restrict(QueryModifiers mod);

//    <RT> SearchResults<RT> listResults(Expr<RT> expr);
}