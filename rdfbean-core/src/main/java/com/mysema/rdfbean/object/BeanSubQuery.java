/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.support.DetachableMixin;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Expr;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.expr.EComparable;
import com.mysema.query.types.expr.EDate;
import com.mysema.query.types.expr.EDateTime;
import com.mysema.query.types.expr.ENumber;
import com.mysema.query.types.expr.EString;
import com.mysema.query.types.expr.ETime;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.query.*;

/**
 * BeanSubQuery is a subquery class for use in BeanQuery instances
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanSubQuery implements Detachable {
    
    private final DetachableMixin detachableMixin;
    
    private final QueryMixin<BeanSubQuery> queryMixin;
    
    public BeanSubQuery() {
        queryMixin = new QueryMixin<BeanSubQuery>(this, new DefaultQueryMetadata());
        detachableMixin = new DetachableMixin(queryMixin);
    }
    
    @Override
    public ObjectSubQuery<Long> count() {
        return detachableMixin.count();
    }
        
    @Override
    public EBoolean exists() {
        return detachableMixin.exists();
    }
    
    /**
     * Defines the sources of the subquery
     * 
     * @param o
     * @return
     */
    public BeanSubQuery from(PEntity<?>... o) {
        return queryMixin.from(o);
    }

    @Override
    public ListSubQuery<Object[]> list(Expr<?> first, Expr<?> second, Expr<?>... rest) {
        return detachableMixin.list(first, second, rest);
    }

    @Override
    public ListSubQuery<Object[]> list(Expr<?>[] args) {
        return detachableMixin.list(args);
    }

    @Override
    public <RT> ListSubQuery<RT> list(Expr<RT> projection) {
        return detachableMixin.list(projection);
    }

    @Override
    public EBoolean notExists() {
        return detachableMixin.notExists();
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

    @Override
    public BooleanSubQuery unique(EBoolean projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> ComparableSubQuery<RT> unique(EComparable<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> DateSubQuery<RT> unique(EDate<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> DateTimeSubQuery<RT> unique(EDateTime<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Number & Comparable<?>> NumberSubQuery<RT> unique(ENumber<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public StringSubQuery unique(EString projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> TimeSubQuery<RT> unique(ETime<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public ObjectSubQuery<Object[]> unique(Expr<?> first, Expr<?> second, Expr<?>... rest) {
        return detachableMixin.unique(first, second, rest);
    }

    @Override
    public ObjectSubQuery<Object[]> unique(Expr<?>[] args) {
        return detachableMixin.unique(args);
    }

    @Override
    public <RT> ObjectSubQuery<RT> unique(Expr<RT> projection) {
        return detachableMixin.unique(projection);
    }

    /**
     * Defines the filter conditions of the subquery
     * 
     * @param o multiple mandatory filters
     * @return
     */
    public BeanSubQuery where(EBoolean... o){
        return queryMixin.where(o);
    }
    

}
