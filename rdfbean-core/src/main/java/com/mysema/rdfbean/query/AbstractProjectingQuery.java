/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.collections15.IteratorUtils;
import org.apache.commons.lang.mutable.MutableInt;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.SearchResults;
import com.mysema.query.collections.LimitingIterator;
import com.mysema.query.support.ProjectableQuery;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Constant;
import com.mysema.query.types.EConstructor;
import com.mysema.query.types.Expr;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NodeType;
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
    L extends N, S> extends ProjectableQuery<SubType>{
    
    protected final Dialect<N,R,B,U,L,S> dialect;
    
    protected final Session session;
    
    @SuppressWarnings("unchecked")
    public AbstractProjectingQuery(Dialect<N,R,B,U,L,S> dialect, Session session) {
        super(new QueryMixin<SubType>(new DefaultQueryMetadata()));
        this.queryMixin.setSelf((SubType) this);
        this.dialect = Assert.notNull(dialect,"dialect");
        this.session = Assert.notNull(session,"session");
    }
    
    protected abstract <RT> RT convert(Class<RT> rt, L node);
        
    public SubType from(PEntity<?>... args){
        return queryMixin.from(args);
    }
    
    @SuppressWarnings("unchecked")
    private <RT> RT getAsProjectionValue(Class<RT> type, N node){
        NodeType nodeType = dialect.getNodeType(node);
        if (nodeType != NodeType.LITERAL){            
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
        
    @SuppressWarnings("unchecked")
    @Nullable
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
            if (node != null){
                return getAsProjectionValue(expr.getType(), node);
            }else{
                return null;
            }
        }
    }
    
    protected abstract Iterator<N[]> getInnerResults();
    
    protected int getIntValue(Constant<Integer> constant){
        return constant.getConstant().intValue();
    }

    protected MappedPath getMappedPathForPropertyPath(Path<?> path){
        PathMetadata<?> md = path.getMetadata();
        MappedClass mc = MappedClass.getMappedClass(md.getParent().getType());   
        return mc.getMappedPath(md.getExpression().toString());    
    }

    public QueryMetadata getMetadata() {
        return queryMixin.getMetadata();
    }
    
    protected R getTypeForDomainClass(Class<?> clazz){        
        MappedClass mc = MappedClass.getMappedClass(clazz);
        if (mc.getUID() != null){
            return dialect.getResource(mc.getUID());
        }else{
            throw new IllegalArgumentException("Got no RDF type for " + clazz.getName());
        }
    }
    
    @Override
    public CloseableIterator<Object[]> iterate(final Expr<?>[] args) {
        queryMixin.addToProjection(args);
        
        // TODO : add batch fetch functionality
        final Iterator<N[]> innerResults = getInnerResults();
        return new CloseableIterator<Object[]>(){
            public boolean hasNext() {
                return innerResults.hasNext();
            }
            public Object[] next() {
                N[] nodes = innerResults.next();
                Object[] rv = new Object[nodes.length];
                MutableInt offset = new MutableInt();
                for (int i = 0; i < rv.length; i++){
                    rv [i] = getAsProjectionValue(args[i], nodes, offset); 
                }
                return rv;
                
            }            
            public void remove() {        
                
            }
            public void close() throws IOException {
                
            }            
        };
    }

    @Override
    public <RT> CloseableIterator<RT> iterate(final Expr<RT> expr) {
        queryMixin.addToProjection(expr);

        // TODO : add batch fetch functionality
        final Iterator<N[]> innerResults = getInnerResults();
        return new CloseableIterator<RT>(){
            public boolean hasNext() {
                return innerResults.hasNext();
            }
            public RT next() {
                N[] nodes = innerResults.next();
                return getAsProjectionValue(expr, nodes, new MutableInt());                               
            }
            public void remove() {
                
            }
            public void close() throws IOException {
                
            }            
        };
    }
    
    @Override
    public <RT> SearchResults<RT> listResults(Expr<RT> expr) {
        // TODO : simplify this
        queryMixin.addToProjection(expr);        
        QueryModifiers modifiers = queryMixin.getMetadata().getModifiers();
        queryMixin.getMetadata().setModifiers(new QueryModifiers(null, null));        
        if (modifiers.isRestricting()){
            Iterator<N[]> iterator = getInnerResults();
            if (iterator.hasNext()){
                List<N[]> total = IteratorUtils.toList(iterator);            
                iterator = LimitingIterator.create(total.iterator(), modifiers);
                if (iterator.hasNext()){
                    List<RT> targetList = new ArrayList<RT>();
                    while (iterator.hasNext()){
                        N[] nodes = iterator.next();
                        targetList.add(getAsProjectionValue(expr, nodes, new MutableInt()));
                    }
                    return new SearchResults<RT>(targetList, modifiers, total.size());
                }else{
                    return new SearchResults<RT>(Collections.<RT>emptyList(), modifiers, total.size());
                }
            }else{
                return SearchResults.emptyResults();
            }    
        }else{
            List<RT> results = list(expr);
            return new SearchResults<RT>(results, Long.MAX_VALUE, 0l, results.size());
        }                
    }

    @Override
    public String toString(){
        return queryMixin.getMetadata().toString();
    }
    
}
