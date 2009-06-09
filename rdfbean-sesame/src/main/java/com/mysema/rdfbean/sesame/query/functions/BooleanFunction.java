package com.mysema.rdfbean.sesame.query.functions;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

import com.mysema.query.types.operation.Operator;

/**
 * BooleanFunction provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
abstract class BooleanFunction extends BaseFunction{
    public BooleanFunction(String uri, Operator<?>...ops){
        super(uri, ops);
    }
    @Override
    public final Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        return valueFactory.createLiteral(convert(args));
    }
    protected abstract boolean convert(Value... args);    
}