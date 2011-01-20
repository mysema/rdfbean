/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.MathExpr.MathOp;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;

/**
 * @author tiwe
 */
public class MathTransformer implements OperationTransformer {
    
    private final Map<Operator<?>, MathOp> ops = new HashMap<Operator<?>,MathOp>();
    
    public MathTransformer(){
        ops.put(Ops.ADD, MathOp.PLUS);
        ops.put(Ops.SUB, MathOp.MINUS);
        ops.put(Ops.MULT, MathOp.MULTIPLY);
        ops.put(Ops.DIV, MathOp.DIVIDE);        
    }

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return ops.keySet();
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        ValueExpr arg1 = context.toValue(operation.getArg(0));
        ValueExpr arg2 = context.toValue(operation.getArg(1));        
        return new MathExpr(arg1, arg2, ops.get(operation.getOperator()));
    }

}
