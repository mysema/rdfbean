/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.collections.CustomQueryable;
import com.mysema.query.collections.impl.EvaluatorFactory;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.Path;

/**
 * Non-optimized Memory based BeanQuery implementation
 * 
 * @author sasa
 *
 */
public class SimpleBeanQuery extends CustomQueryable<SimpleBeanQuery> implements Closeable, BeanQuery {

    private final Session session;
    
    public SimpleBeanQuery(final Session session) {
        super(new DefaultQueryMetadata(), EvaluatorFactory.DEFAULT);
        this.session = session;
    }
       
    @Override
    public void close(){
//        session.close();
    }

    @Override
    public BeanQuery from(PEntity<?>... o) {
        return super.from(o);
    }

    @Override
    public BeanQuery limit(long limit) {
        getMetadata().setLimit(limit);
        return this;
    }

    @Override
    public BeanQuery offset(long offset) {
        getMetadata().setOffset(offset);
        return this;
    }

    @Override
    public BeanQuery restrict(QueryModifiers mod) {
        getMetadata().setModifiers(mod);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> Iterable<T> getContent(Path<T> expr) {
        return (Iterable<T>)session.findInstances(expr.getType());
    }
    
}
