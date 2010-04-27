/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Compare.CompareOp;

import com.mysema.query.types.Constant;
import com.mysema.query.types.Expr;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.SubQuery;

/**
 * CompareTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CompareTransformer implements OperationTransformer{
    
    private static final Map<Operator<?>,CompareOp> ops = new HashMap<Operator<?>,CompareOp>();
    
    private final OperationTransformer colSize = new ColSizeTransformer();
    
    static{
        ops.put(Ops.LT, CompareOp.LT);
        ops.put(Ops.BEFORE, CompareOp.LT);
        ops.put(Ops.LOE, CompareOp.LE);
        ops.put(Ops.BOE, CompareOp.LE);
        ops.put(Ops.GT, CompareOp.GT);
        ops.put(Ops.AFTER, CompareOp.GT);
        ops.put(Ops.GOE, CompareOp.GE);
        ops.put(Ops.AOE, CompareOp.GE);
    }

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return ops.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        Expr<?> arg1 = operation.getArg(0);
        Expr<?> arg2 = operation.getArg(1);
        CompareOp op = ops.get(operation.getOperator());
        
        if (arg2 instanceof SubQuery){
            ValueExpr lhs = context.toValue(arg1);
            TupleExpr rhs = context.toTuples((SubQuery) arg2);
            return new CompareAll(lhs, rhs, op);
            
        }else if (arg1 instanceof Operation
                && ((Operation)arg1).getOperator() == Ops.COL_SIZE
                && arg2 instanceof Constant){
            return colSize.transform(operation, context);
            
        }else{
            ValueExpr lhs = context.toValue(arg1);
            ValueExpr rhs = context.toValue(arg2);
            return new Compare(lhs, rhs, op);    
        }
        
    }
}
