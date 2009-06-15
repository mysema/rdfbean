/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.List;

import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Compare.CompareOp;

/**
 * CompareTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
class CompareTransformer implements Transformer{
    private final CompareOp op;        
    
    CompareTransformer(CompareOp op){
        this.op = op;
    }       
    
    @Override
    public ValueExpr transform(List<ValueExpr> args){
        return new Compare(args.get(0), args.get(1), op);
    }        
}