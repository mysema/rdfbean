package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.query.algebra.ValueExpr;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.PathType;

/**
 * DelegateTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DelegateTransformer implements OperationTransformer{

    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(PathType.DELEGATE);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        return context.toValue(operation.getArg(0));
    }

}
