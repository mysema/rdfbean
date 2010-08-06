/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Constant;
import com.mysema.query.types.Expr;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.OBoolean;

/**
 * InTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class InTransformer implements OperationTransformer{

    private final EqualsTransformer equals = new EqualsTransformer();
    
    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(Ops.IN);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        Expr<?> arg1 = operation.getArg(0);
        Expr<?> arg2 = operation.getArg(1);
        
        // path in path
        if (arg1 instanceof Path && arg2 instanceof Path){
            return pathInPath(operation, context, arg1, arg2);
            
        // const in path    
        }else if (arg1 instanceof Constant && arg2 instanceof Path){
            return constInPath(operation, context, arg1, arg2);
            
        // path in collection    
        }else if (arg1 instanceof Path && arg2 instanceof Constant){
            return pathInCollection(context, arg1, arg2);
            
        }else{
            throw new IllegalArgumentException(operation.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private ValueExpr pathInCollection(TransformerContext context,
            Expr<?> arg1, Expr<?> arg2) {
        Expr<Object> expr = (Expr<Object>)arg1;
        Constant<?> constant = (Constant<?>)arg2;
        Collection<?> collection = (Collection<?>)constant.getConstant();
        if (collection.isEmpty()){
            throw new IllegalArgumentException("Empty collection not allowed : " + arg1 + " in {}"); 
        }
        BooleanBuilder bo = new BooleanBuilder();
        for (Object elem : collection){
            bo.or(expr.eq(elem));
        }               
        return context.toValue(bo);
    }

    @Nullable
    private ValueExpr constInPath(Operation<?> operation,
            TransformerContext context, Expr<?> arg1, Expr<?> arg2) {
        // TODO : make const in path work for RDF sequences and containers
        if (!context.inNegation() && !context.inOptionalPath()){
            Var var = context.toVar((Constant<?>) arg1);
            Var path = context.toVar((Path<?>) arg2);
            path.setValue(var.getValue());
            return null;    
        }else{
            return equals.transform((Operation<?>) OBoolean.create(Ops.EQ_OBJECT, arg1, arg2), context);
        }
    }

    @Nullable
    private ValueExpr pathInPath(Operation<?> operation,
            TransformerContext context, Expr<?> arg1, Expr<?> arg2) {
        // TODO : make path in path work for RDF sequences and containers
        Path<?> path = (Path<?>) arg1;
        if (!context.inNegation() && !context.inOptionalPath()){
            Path<?> otherPath = (Path<?>) arg2;
            context.register(otherPath, context.toVar(path));
            context.toVar(otherPath);
            return null;
        }else{
            return equals.transform((Operation<?>) OBoolean.create(Ops.EQ_OBJECT, arg1, arg2), context);
        }
    }

}
