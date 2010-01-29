/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;

import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.expr.Constant;
import com.mysema.query.types.operation.Operation;
import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.query.types.path.Path;
import com.mysema.rdfbean.model.RDF;

/**
 * ColSizeTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ColSizeTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?, ?> operation, TransformerContext context) {
        Operator<?> op = operation.getOperator();
        int size = ((Constant<Integer>) operation.getArg(1)).getConstant().intValue();
        if (op == Ops.GOE){
            op = Ops.GT;
            size--;
        }else if (op == Ops.LOE){
            op = Ops.LT;
            size++;
        }
        
        JoinBuilder builder = context.createJoinBuilder();
        // path from size operation
        Path<?> path = (Path<?>)((Operation<?,?>)operation.getArg(0)).getArg(0); 
        Var pathVar = context.toVar(path);                                
        for (int i=0; i < size-1; i++){
            Var rest = context.createVar();
            context.match(builder, pathVar, RDF.rest, rest);
            pathVar = rest;
        }
        
        // last
        if (op == Ops.EQ_PRIMITIVE){
            context.match(builder, pathVar, RDF.rest, context.toVar(RDF.nil));
            
        }else if (op == Ops.GT){
            Var next = context.createVar();
            context.match(builder, pathVar, RDF.rest, next);
            context.match(builder, next, RDF.rest, context.createVar());
            
        }else if (op == Ops.LT){
            context.match(builder, pathVar, RDF.rest, context.createVar());
        }          
        
        if (op != Ops.LT){
            return new Exists(builder.getTupleExpr());    
        }else{
            return new Not(new Exists(builder.getTupleExpr()));
        }    
    }

}
