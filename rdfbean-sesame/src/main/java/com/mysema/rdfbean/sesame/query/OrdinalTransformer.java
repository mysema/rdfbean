package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;

import org.openrdf.query.algebra.ValueExpr;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;

public class OrdinalTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(Ops.ORDINAL);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}
