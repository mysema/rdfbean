/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.mutable.MutableInt;

import com.mysema.commons.lang.Assert;
import com.mysema.query.QueryBaseWithProjection;
import com.mysema.query.grammar.types.Expr;
import com.mysema.query.grammar.types.Path;
import com.mysema.query.grammar.types.PathMetadata;
import com.mysema.query.grammar.types.Expr.EConstant;
import com.mysema.query.grammar.types.Expr.EConstructor;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.Session;


/**
 * Base class for projecting RDF results to Java typed results
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractProjectingQuery<SubType extends AbstractProjectingQuery<SubType,N,R,B,U,L,S>, 
    N, 
    R extends N, 
    B extends R, 
    U extends R, 
    L extends N, S> extends QueryBaseWithProjection<Object,SubType>{
    
    protected final Dialect<N,R,B,U,L,S> dialect;
    
    protected final Session session;
    
    public AbstractProjectingQuery(Dialect<N,R,B,U,L,S> dialect, Session session) {
        this.dialect = Assert.notNull(dialect);
        this.session = Assert.notNull(session);
    }
        
    protected abstract Iterator<N[]> getInnerResults();
    
    protected int getIntValue(EConstant<Integer> constant){
        return constant.getConstant().intValue();
    }
    
    protected MappedPath getMappedPathForPropertyPath(Path<?> path){
        PathMetadata<?> md = path.getMetadata();
        MappedClass mc = MappedClass.getMappedClass(md.getParent().getType());   
        return mc.getMappedPath(md.getExpression().toString());    
    }
        
    protected R getTypeForJavaClass(Class<?> clazz){
        MappedClass mc = MappedClass.getMappedClass(clazz);
        if (mc.getUID() != null){
            return dialect.getResource(mc.getUID());
        }else{
            throw new IllegalArgumentException("Got no RDF type for " + clazz.getName());
        }
    }
    
    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <RT> Iterator<RT> iterate(final Expr<RT> expr) {
        addToProjection(expr);
        
        final Iterator<N[]> innerResults = getInnerResults();
        return new Iterator<RT>(){
            public boolean hasNext() {
                return innerResults.hasNext();
            }
            public RT next() {
                N[] nodes = innerResults.next();
                return getAsProjectionValue(expr, nodes, new MutableInt());                               
            }
            public void remove() {                
            }            
        };
    }
    
    @Override
    public Iterator<Object[]> iterate(final Expr<?> first, final Expr<?> second, final Expr<?>... rest) {
        addToProjection(first, second);
        addToProjection(rest);
        
        final Iterator<N[]> innerResults = getInnerResults();
        return new Iterator<Object[]>(){
            public boolean hasNext() {
                return innerResults.hasNext();
            }
            public Object[] next() {
                N[] nodes = innerResults.next();
                Object[] rv = new  Object[nodes.length];
                MutableInt offset = new MutableInt();
                rv[0] = getAsProjectionValue(first, nodes, offset);          
                rv[1] = getAsProjectionValue(second, nodes, offset);         
                for (int i = 2; i < rv.length; i++){
                    rv [i] = getAsProjectionValue(rest[i-2], nodes, offset); 
                }
                return rv;
                
            }            
            public void remove() {                
            }            
        };
    }
    
    @SuppressWarnings("unchecked")
    private <RT> RT getAsProjectionValue(Expr<RT> expr, N[] nodes, MutableInt offset) {
        if (expr instanceof EConstructor){
            EConstructor<?> constructor = (EConstructor<?>)expr;
            Object[] args = new Object[constructor.getArgs().size()];
            for (int i = 0; i < args.length; i++){
                args[i] = getAsProjectionValue(constructor.getArg(i).getType(), nodes[offset.intValue() + i]);
            }
            offset.add(args.length);
            try {
                return (RT) constructor.getJavaConstructor().newInstance(args);
            } catch (Exception e) {            
                throw new RuntimeException(e.getMessage(), e);
            }
        }else{
            N node = nodes[offset.intValue()];
            offset.add(1);
            return getAsProjectionValue(expr.getType(), node);                
        }
    }
    
    @SuppressWarnings("unchecked")
    private <RT> RT getAsProjectionValue(Class<RT> type, N node){
        if (dialect.isResource(node)){
            ID id = dialect.getID((R)node);
            if (type.equals(String.class)){
                // TODO : always return LID ?
                return (RT) session.getLID(id).getId();
            }else{
                return (RT) session.get(type, id);    
            }            
        }else{            
            return convert(type, (L)node);
        }
    }

    protected abstract <RT> RT convert(Class<RT> rt, L node);
    
    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this);
    }
    
}
