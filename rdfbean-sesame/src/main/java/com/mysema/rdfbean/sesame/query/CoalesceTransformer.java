package com.mysema.rdfbean.sesame.query;

import java.util.Collection;
import java.util.Collections;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;

/**
 * CoalesceTransformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CoalesceTransformer implements OperationTransformer{

    static{
        FunctionRegistry.getInstance().add(new Function(){
            @Override
            public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
                for (Value value : args){
                    if (value != null){
                        return value;
                    }
                }
                return null;
            }

            @Override
            public String getURI() {
                return "functions:coalesce";
            }
            
        });
    }
    
    @Override
    public Collection<? extends Operator<?>> getSupportedOperations() {
        return Collections.singleton(Ops.COALESCE);
    }

    @Override
    public ValueExpr transform(Operation<?> operation, TransformerContext context) {
        Operation<?> list = (Operation<?>) operation.getArg(0);
        ValueExpr[] values = new ValueExpr[list.getArgs().size()];
        for (int i = 0; i < values.length; i++){
            values[i] = context.toValue(list.getArg(i));
        }        
        return new FunctionCall("functions:coalesce", values);
    }

}
