/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Path;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;

/**
 * InstanceOfTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class InstanceOfTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(Ops.INSTANCE_OF);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        if (context.inNegation() || context.inOptionalPath()){
            StatementPattern pattern = new StatementPattern(
                    context.getPatternScope(),
                    (Var)context.toValue(operation.getArg(0)),
                    context.toVar(RDF.type),
                    (Var)context.toValue(operation.getArg(1)));
            return new Exists(pattern);    
            
        }else{
            UID c = null;
            if (operation.getArg(0) instanceof Path<?>){
                c = context.getContext((Path<?>) operation.getArg(0));
            }
            context.match(
                (Var)context.toValue(operation.getArg(0)),
                RDF.type,
                (Var)context.toValue(operation.getArg(1)),
                c); 
            return null;
        }        
    }


}
