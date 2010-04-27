/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Path;
import com.mysema.rdfbean.model.RDF;

/**
 * ColIsEmptyTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ColIsEmptyTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(Ops.COL_IS_EMPTY);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        Var pathVar = context.toVar((Path<?>)operation.getArg(0));            
        if (context.inNegation()){
            return new Compare(pathVar, context.toVar(RDF.nil), Compare.CompareOp.EQ);    
        }else{
            pathVar.setValue(context.toValue(RDF.nil));
            return null;    
        }
    }

}
