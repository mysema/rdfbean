/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;
import java.io.IOException;

import com.mysema.query.QueryModifiers;
import com.mysema.query.support.ProjectableAdapter;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.query.AbstractProjectingQuery;

/**
 * BeanQueryAdapter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanQueryAdapter extends ProjectableAdapter implements BeanQuery{

    private final AbstractProjectingQuery<?,?,?,?,?,?,?> query;
    
    private final Closeable closeable;
    
    public BeanQueryAdapter(AbstractProjectingQuery<?,?,?,?,?,?,?> query, Closeable closeable){
        super(query);
        this.query = query;
        this.closeable = closeable;
    }
    
    @Override
    public BeanQuery from(PEntity<?>... o) {
        query.from(o);
        return this;
    }

    @Override
    public BeanQuery orderBy(OrderSpecifier<?>... o) {
        query.orderBy(o);
        return this;
    }

    @Override
    public BeanQuery where(EBoolean... o) {
        query.where(o);
        return this;
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }

    @Override
    public BeanQuery restrict(QueryModifiers mod) {
        query.getMetadata().setModifiers(mod);
        return this;
    }

    @Override
    public BeanQuery limit(long limit) {
        query.getMetadata().setLimit(limit);
        return this;
    }

    @Override
    public BeanQuery offset(long offset) {
        query.getMetadata().setOffset(offset);
        return this;
    }
    
    public String toString(){
        return query.toString();
    }

}
