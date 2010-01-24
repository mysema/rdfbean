/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.types.operation.Ops.AFTER;
import static com.mysema.query.types.operation.Ops.AOE;
import static com.mysema.query.types.operation.Ops.BEFORE;
import static com.mysema.query.types.operation.Ops.BOE;
import static com.mysema.query.types.operation.Ops.GOE;
import static com.mysema.query.types.operation.Ops.GT;
import static com.mysema.query.types.operation.Ops.LOE;
import static com.mysema.query.types.operation.Ops.LT;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Compare.CompareOp;

import com.mysema.query.types.expr.Constant;
import com.mysema.query.types.expr.Expr;
import com.mysema.query.types.operation.Operation;
import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.query.types.query.SubQuery;

/**
 * CompareTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CompareTransformer implements Transformer{
    
    private final Map<Operator<?>,CompareOp> ops = new HashMap<Operator<?>,CompareOp>();
    
    private final Transformer colSize = new ColSizeTransformer();
    
    {
        ops.put(LT, CompareOp.LT);
        ops.put(BEFORE, CompareOp.LT);
        ops.put(LOE, CompareOp.LE);
        ops.put(BOE, CompareOp.LE);
        ops.put(GT, CompareOp.GT);
        ops.put(AFTER, CompareOp.GT);
        ops.put(GOE, CompareOp.GE);
        ops.put(AOE, CompareOp.GE);
    }

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return ops.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?, ?> operation, TransformerContext context) {
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
