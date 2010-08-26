package com.mysema.rdfbean.sesame.query;

import java.util.Arrays;
import java.util.Collection;

import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Path;
import com.mysema.rdfbean.CORE;

public class OrdinalTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Arrays.<Operator<?>>asList(Ops.ORDINAL);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        Var var = context.toVar((Path<?>)operation.getArg(0));
        Var ordinal = new Var(var.getName() + "_ordinal");
        context.match(var, CORE.enumOrdinal, ordinal);
        return ordinal;
    }

}
