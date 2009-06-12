package com.mysema.rdfbean.sesame.query.functions;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

import com.mysema.query.types.operation.Operator;
import com.mysema.rdfbean.model.UID;

/**
 * IntegerFunction provides
 * 
 * @author tiwe
 * @version $Id$
 * 
 */
abstract class IntegerFunction extends BaseFunction {
    public IntegerFunction(UID uri, Operator<?>... ops) {
        super(uri, ops);
    }

    @Override
    public final Value evaluate(ValueFactory valueFactory, Value... args)
            throws ValueExprEvaluationException {
        return valueFactory.createLiteral(convert(args));
    }

    protected abstract int convert(Value... args);
}