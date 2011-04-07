/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.support.DetachableAdapter;
import com.mysema.query.support.DetachableMixin;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;

/**
 * BeanSubQuery is a subquery class for use in BeanQuery instances
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanSubQuery extends DetachableAdapter {

    private final QueryMixin<BeanSubQuery> queryMixin;

    public BeanSubQuery() {
        queryMixin = new QueryMixin<BeanSubQuery>(this, new DefaultQueryMetadata(false));
        setDetachable(new DetachableMixin(queryMixin));
    }

    /**
     * Defines the sources of the subquery
     *
     * @param o
     * @return
     */
    public BeanSubQuery from(EntityPath<?>... o) {
        return queryMixin.from(o);
    }

    /**
     * Defines the order of the subquery
     *
     * @param o
     * @return
     */
    public BeanSubQuery orderBy(OrderSpecifier<?>... o){
        return queryMixin.orderBy(o);
    }

    /**
     * Defines the filter conditions of the subquery
     *
     * @param o multiple mandatory filters
     * @return
     */
    public BeanSubQuery where(Predicate... o){
        return queryMixin.where(o);
    }


}
