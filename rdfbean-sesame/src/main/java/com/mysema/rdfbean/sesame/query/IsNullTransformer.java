/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;

import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Path;

/**
 * @author tiwe
 */
public class IsNullTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(Ops.IS_NULL, Ops.IS_NOT_NULL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        if (operation.getArg(0) instanceof Path){
            Var arg = context.toVar((Path)operation.getArg(0));
            if (operation.getOperator() == Ops.IS_NULL){
                return new Not(new Bound(arg));
                
            }else{
                return new Bound(arg);
            }
            
        }else{
            throw new IllegalArgumentException(operation.toString());
        }
        
    }

}
