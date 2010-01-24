/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.operation.Operation;
import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.query.types.path.Path;

/**
 * MapIsEmptyTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MapIsEmptyTransformer implements Transformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(Ops.MAP_ISEMPTY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr transform(Operation<?, ?> operation, TransformerContext context) {
        if (operation.getArg(0) instanceof Path){
            Var arg = context.toVar((Path)operation.getArg(0));
            return new Not(new Bound(arg));    
        }else{
            throw new IllegalArgumentException(operation.toString());
        }
        
    }

}
