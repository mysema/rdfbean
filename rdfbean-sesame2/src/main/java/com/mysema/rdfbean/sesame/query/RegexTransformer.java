/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;

import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.ValueExpr;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;

/**
 * RegexTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RegexTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(Ops.STRING_IS_EMPTY, Ops.MATCHES);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        ValueExpr arg1 = context.toValue(operation.getArg(0));
        
        if (operation.getOperator() == Ops.STRING_IS_EMPTY){
            return new Like(new Str(arg1), "", true);
            
        }else{
            ValueExpr arg2 = context.toValue(operation.getArg(1));
            return new Regex(new Str(arg1), new Str(arg2), null);
        }
                 
    }

}
