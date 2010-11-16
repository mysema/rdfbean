/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Compare.CompareOp;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;

/**
 * BetweenTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BetweenTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(Ops.BETWEEN);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        ValueExpr arg1 = context.toValue(operation.getArg(0));
        ValueExpr arg2 = context.toValue(operation.getArg(1));
        
        return new And(
              new Compare(arg1, arg2, CompareOp.GE),    
              new Compare(arg1, arg2, CompareOp.LE));
    }

}
