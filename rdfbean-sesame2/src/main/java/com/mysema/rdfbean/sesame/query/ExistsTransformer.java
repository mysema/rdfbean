/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.SubQuery;

/**
 * ExistsTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ExistsTransformer implements OperationTransformer{

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        if (operation.getArg(0) instanceof SubQuery){
            SubQuery subQuery = (SubQuery) operation.getArg(0);
            TupleExpr tupleExpr = context.toTuples(subQuery);
            return new Exists(tupleExpr);    
        }else{
            throw new IllegalArgumentException("Illegal expression " + operation);
        }        
    }

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(Ops.EXISTS);
    }

}
