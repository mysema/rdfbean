/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.collections15.Transformer;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;

import com.mysema.commons.lang.Assert;

/**
 * JoinBuilder provides
 *
 * @author tiwe
 * @version $Id$
 */
public class JoinBuilder{
    
    private final Transformer<StatementPattern,TupleExpr> stmtTransformer;
    
    private final List<StatementPattern> patterns = new ArrayList<StatementPattern>();
    
    private TupleExpr tupleExpr;
    
    public JoinBuilder(Transformer<StatementPattern,TupleExpr> stmtTransformer){
        this.stmtTransformer = Assert.notNull(stmtTransformer);
    }

    public JoinBuilder add(StatementPattern pattern){
        patterns.add(pattern);        
        return this;
    }
        
    public TupleExpr getTupleExpr() {
        if (!patterns.isEmpty()){
            tupleExpr = merge(patterns, tupleExpr);
        }        
        return tupleExpr;
    }
    
    public boolean isEmpty() {
        return tupleExpr == null && patterns.isEmpty();
    }
    
    private TupleExpr merge(List<StatementPattern> patterns, @Nullable TupleExpr base){
        TupleExpr rv = base;
        for (StatementPattern pattern : patterns){
            if (rv != null){
                rv = new Join(rv, stmtTransformer.transform(pattern));
            }else{
                rv = stmtTransformer.transform(pattern);
            }
        }
        patterns.clear();
        return rv;        
    }
    
    public void setMandatory(){
        if (!patterns.isEmpty()){        
            tupleExpr = new LeftJoin(tupleExpr, merge(patterns, null)); 
            patterns.clear();
        }        
    }    
        
    public void setOptional(){
        if (!patterns.isEmpty()){
            tupleExpr = merge(patterns, tupleExpr);
        }        
    }
    
}