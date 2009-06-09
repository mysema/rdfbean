package com.mysema.rdfbean.sesame.query.functions;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

import com.mysema.query.types.operation.Operator;

/**
 * StringFunction provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
abstract class StringFunction extends BaseFunction{
    public StringFunction(String uri, Operator<?>...ops){
        super(uri, ops);
    }
    @Override
    public final Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        return valueFactory.createLiteral(convert(args));
    }
    protected abstract String convert(Value... args);
}