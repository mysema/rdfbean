/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.support.DetachableMixin;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.ComparableExpression;
import com.mysema.query.types.expr.DateExpression;
import com.mysema.query.types.expr.DateTimeExpression;
import com.mysema.query.types.expr.NumberExpression;
import com.mysema.query.types.expr.StringExpression;
import com.mysema.query.types.expr.TimeExpression;
import com.mysema.query.types.query.*;

/**
 * BeanSubQuery is a subquery class for use in BeanQuery instances
 *
 * @author tiwe
 * @version $Id$
 */
// TODO : use DetachableAdapter
public class BeanSubQuery implements Detachable {
    
    private final DetachableMixin detachableMixin;
    
    private final QueryMixin<BeanSubQuery> queryMixin;
    
    public BeanSubQuery() {
        queryMixin = new QueryMixin<BeanSubQuery>(this, new DefaultQueryMetadata());
        detachableMixin = new DetachableMixin(queryMixin);
    }
    
    @Override
    public SimpleSubQuery<Long> count() {
        return detachableMixin.count();
    }
        
    @Override
    public BooleanExpression exists() {
        return detachableMixin.exists();
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

    @Override
    public ListSubQuery<Object[]> list(Expression<?> first, Expression<?> second, Expression<?>... rest) {
        return detachableMixin.list(first, second, rest);
    }

    @Override
    public ListSubQuery<Object[]> list(Expression<?>[] args) {
        return detachableMixin.list(args);
    }

    @Override
    public <RT> ListSubQuery<RT> list(Expression<RT> projection) {
        return detachableMixin.list(projection);
    }

    @Override
    public BooleanExpression notExists() {
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
    public BooleanSubQuery unique(Predicate projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> ComparableSubQuery<RT> unique(ComparableExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> DateSubQuery<RT> unique(DateExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> DateTimeSubQuery<RT> unique(DateTimeExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Number & Comparable<?>> NumberSubQuery<RT> unique(NumberExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public StringSubQuery unique(StringExpression projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public <RT extends Comparable<?>> TimeSubQuery<RT> unique(TimeExpression<RT> projection) {
        return detachableMixin.unique(projection);
    }

    @Override
    public SimpleSubQuery<Object[]> unique(Expression<?> first, Expression<?> second, Expression<?>... rest) {
        return detachableMixin.unique(first, second, rest);
    }

    @Override
    public SimpleSubQuery<Object[]> unique(Expression<?>[] args) {
        return detachableMixin.unique(args);
    }

    @Override
    public <RT> SimpleSubQuery<RT> unique(Expression<RT> projection) {
        return detachableMixin.unique(projection);
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
