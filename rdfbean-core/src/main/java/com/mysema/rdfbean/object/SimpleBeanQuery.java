/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;
import java.util.Iterator;

import com.mysema.query.collections.IteratorSource;
import com.mysema.query.collections.support.CustomQueryable;
import com.mysema.query.grammar.types.Expr;

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
    
}
