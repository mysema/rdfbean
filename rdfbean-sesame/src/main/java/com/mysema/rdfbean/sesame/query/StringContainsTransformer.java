/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.operation.Operation;
import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;

/**
 * StringContainsTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class StringContainsTransformer implements Transformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(
                Ops.ENDS_WITH, 
                Ops.ENDS_WITH_IC,
                Ops.STRING_CONTAINS, 
                Ops.STRING_CONTAINS_IC, 
                Ops.STARTS_WITH, 
                Ops.STARTS_WITH_IC);
    }

    @Override
    public ValueExpr transform(Operation<?, ?> operation, TransformerContext context) {
        List<ValueExpr> args = Arrays.asList(
                context.toValue(operation.getArg(0)), 
                context.toValue(operation.getArg(1)));
        Operator<?> op = operation.getOperator();
        
        if (op == Ops.STRING_CONTAINS){
            return stringContains(args, true);
            
        }else if (op == Ops.STRING_CONTAINS_IC){
            return stringContains(args, false);
            
        }else if (op == Ops.STARTS_WITH){
            return startsWith(args, true);
            
        }else if (op == Ops.STARTS_WITH_IC){
            return startsWith(args, false);
            
        }else if (op == Ops.ENDS_WITH){
            return endsWith(args, true);   
            
        }else if (op == Ops.ENDS_WITH_IC){
            return endsWith(args, false);
            
        }else{
            throw new IllegalArgumentException(operation.toString());
        }
    }

    private ValueExpr endsWith(List<ValueExpr> args, boolean caseSensitive) {
        ValueExpr first = new Str(args.get(0));
        if (args.get(1) instanceof Var){
            Var arg2 = ((Var)args.get(1));
            if (arg2.getValue() != null){
                return new Regex(first, "*"+((Var)arg2).getValue().stringValue(),caseSensitive); 
            }    
        }
        if (caseSensitive){
            return new FunctionCall("functions:endsWith", args);    
        }else{
            return new FunctionCall("functions:endsWithIc", args);
        }
        
    }

    private ValueExpr startsWith(List<ValueExpr> args, boolean caseSensitive) {
        ValueExpr first = new Str(args.get(0));
        if (args.get(1) instanceof Var){
            Var arg2 = ((Var)args.get(1));
            if (arg2.getValue() != null){
                return new Regex(first, ((Var)arg2).getValue().stringValue()+"*", caseSensitive);
            }    
        }
        if (caseSensitive){
            return new FunctionCall("functions:startsWith", args);
        }else{
            return new FunctionCall("functions:startsWithIc", args);    
        }
        
    }

    private ValueExpr stringContains(List<ValueExpr> args, boolean caseSensitive) {
        ValueExpr first = new Str(args.get(0));
        if (args.get(1) instanceof Var){
            Var arg2 = ((Var)args.get(1));
            if (arg2.getValue() != null){
                return new Regex(first, "*"+((Var)arg2).getValue().stringValue()+"*",caseSensitive);    
            }    
        }
        if (caseSensitive){
            return new FunctionCall("functions:stringContains", args);    
        }else{
            return new FunctionCall("functions:stringContainsIc", args);
        }
        
    }

}
