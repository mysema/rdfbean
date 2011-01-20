/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;

import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;

/**
 * @author tiwe
 */
public class CastTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(Ops.STRING_CAST, Ops.NUMCAST);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        ValueExpr arg1 = context.toValue(operation.getArg(0));
        
        if (operation.getOperator() == Ops.STRING_CAST){
            return new Str(arg1);   
        }else{
            ValueExpr arg2 = context.toValue(operation.getArg(1));
            return new FunctionCall( ((Var)arg2).getValue().stringValue(), arg1);    
        }        
        
    }

}
