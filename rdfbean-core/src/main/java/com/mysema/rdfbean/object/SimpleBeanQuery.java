/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;
import java.util.Collection;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.collections.ColQuery;
import com.mysema.query.collections.ColQueryImpl;
import com.mysema.query.collections.QueryEngine;
import com.mysema.query.support.ProjectableAdapter;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expr;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Param;
import com.mysema.query.types.expr.EBoolean;

/**
 * Non-optimized Memory based BeanQuery implementation
 * 
 * @author sasa
 *
 */
public class SimpleBeanQuery extends ProjectableAdapter<ColQueryImpl> implements Closeable, BeanQuery {
    
    private final Session session;
    
    private final ColQuery colQuery;
    
    public SimpleBeanQuery(Session session) {
        this(session, new DefaultQueryMetadata());
    }
    
    protected SimpleBeanQuery(Session session, QueryMetadata metadata){
        super(new ColQueryImpl(metadata, QueryEngine.DEFAULT));
        this.session = session;
        this.colQuery = (ColQuery) super.getProjectable();
    }
       
    @Override
    public void close(){
//        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public BeanQuery from(EntityPath<?>... o) {
        for (EntityPath<?> path : o){
            colQuery.from(path, (Collection)session.findInstances(path.getType()));
        }
        return this;
    }

    @Override
    public BeanQuery limit(long limit) {
        colQuery.limit(limit);
        return this;
    }

    @Override
    public BeanQuery offset(long offset) {
        colQuery.offset(offset);
        return this;
    }

    @Override
    public BeanQuery restrict(QueryModifiers mod) {
        colQuery.restrict(mod);
        return this;
    }

    @Override
    public BeanQuery orderBy(OrderSpecifier<?>... o) {
        colQuery.orderBy(o);
        return this;
    }

    @Override
    public BeanQuery where(EBoolean... o) {
        colQuery.where(o);
        return this;
    }

    @Override
    public BeanQuery groupBy(Expr<?>... o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BeanQuery having(EBoolean... o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> BeanQuery set(Param<T> param, T value) {
        colQuery.set(param, value);
        return this;
    }

    
}
