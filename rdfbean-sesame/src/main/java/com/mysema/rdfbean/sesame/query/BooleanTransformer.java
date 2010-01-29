/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.ValueExpr;

import com.mysema.query.types.expr.Expr;
import com.mysema.query.types.operation.Operation;
import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;

/**
 * BooleanTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BooleanTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(Ops.AND, Ops.OR, Ops.NOT);                
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?, ?> operation, TransformerContext context) {
        Operator<?> op = operation.getOperator();
        List<Expr<?>> args = operation.getArgs();
        
        if (op == Ops.AND){
            ValueExpr arg1, arg2;
            // mandatory paths before optional paths
            if (args.get(0) instanceof Operation && ((Operation)args.get(0)).getOperator() == Ops.OR){
                arg1 = context.toValue(args.get(1));
                arg2 = context.toValue(args.get(0));
            }else{
                arg1 = context.toValue(args.get(0));
                arg2 = context.toValue(args.get(1));
            }
            if (arg1 == null){
                return arg2;                
            }else if (arg2 == null){
                return arg1;                
            }else{
                return new And(arg1, arg2);  
            }  
            
        }else if (op == Ops.OR){
            ValueExpr arg1 = context.toValue(args.get(0));
            ValueExpr arg2 = context.toValue(args.get(1));
            return new Or(arg1, arg2);
            
        }else if (op == Ops.NOT){
            return new Not(context.toValue(args.get(0)));
            
        }else{
            throw new IllegalArgumentException(operation.toString());
        }
    }
    
}
