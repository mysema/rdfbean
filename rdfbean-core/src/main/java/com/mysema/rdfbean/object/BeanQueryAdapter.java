/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;
import java.io.IOException;

import com.mysema.query.ProjectableAdapter;
import com.mysema.query.QueryBaseWithProjection;
import com.mysema.query.grammar.OrderSpecifier;
import com.mysema.query.grammar.types.Expr;
import com.mysema.query.grammar.types.Expr.EBoolean;

/**
 * BeanQueryAdapter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanQueryAdapter extends ProjectableAdapter implements BeanQuery{

    private final QueryBaseWithProjection<?,?> query;
    
    private final Closeable closeable;
    
    public BeanQueryAdapter(QueryBaseWithProjection<?,?> query, Closeable closeable){
        super(query);
        this.query = query;
        this.closeable = closeable;
    }
    
    @Override
    public BeanQuery from(Expr<?>... o) {
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

}
