/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;
import java.util.Iterator;

import com.mysema.query.QueryModifiers;
import com.mysema.query.collections.IteratorSource;
import com.mysema.query.collections.support.CustomQueryable;
import com.mysema.query.types.expr.Expr;
import com.mysema.query.types.path.PEntity;

/**
 * ColQuery based BeanQuery implementation
 * 
 * @author sasa
 *
 */
public class SimpleBeanQuery extends CustomQueryable<SimpleBeanQuery> implements Closeable, BeanQuery {

//    private Session session;
    
    public SimpleBeanQuery(final Session session) {
        super(new IteratorSource(){
            @SuppressWarnings("unchecked")
            @Override
            public <A> Iterator<A> getIterator(Expr<A> expr) {
                return (Iterator<A>)session.findInstances(expr.getType()).iterator();
            }
            @Override
            public <A> Iterator<A> getIterator(Expr<A> expr, Object[] bindings) {
                return getIterator(expr);
            }
            
        });
//        this.session = session;
    }
        
    /* (non-Javadoc)
     * @see com.mysema.rdfbean.object.BeanQuery#close()
     */
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
        getMetadata().getModifiers().setLimit(limit);
        return this;
    }

    @Override
    public BeanQuery offset(long offset) {
        getMetadata().getModifiers().setOffset(offset);
        return this;
    }

    @Override
    public BeanQuery restrict(QueryModifiers mod) {
        getMetadata().setModifiers(mod);
        return this;
    }
    
}
