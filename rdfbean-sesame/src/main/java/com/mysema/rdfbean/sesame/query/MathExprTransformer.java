/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.List;

import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.ValueExpr;

/**
 * MathExprTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
class MathExprTransformer implements Transformer{
    private final MathExpr.MathOp op;        
    
    MathExprTransformer(MathExpr.MathOp op){
        this.op = op;
    }       
    
    @Override
    public ValueExpr transform(List<ValueExpr> args){
        return new MathExpr(args.get(0), args.get(1), op);
    }        
}