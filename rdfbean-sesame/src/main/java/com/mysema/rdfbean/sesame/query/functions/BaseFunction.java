package com.mysema.rdfbean.sesame.query.functions;

import org.openrdf.query.algebra.evaluation.function.Function;

import com.mysema.query.types.operation.Operator;
import com.mysema.rdfbean.query.Constants;

/**
 * BaseFunction provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
abstract class BaseFunction implements Function{
    private final String uri;
    private final Operator<?>[] ops;
    public BaseFunction(String ln, Operator<?>...ops){
        this.uri = Constants.NS + ln;
        this.ops = ops;
    }
    
    @Override
    public final String getURI() {
        return uri;
    }        
    public final Operator<?>[] getOps(){
        return ops;
    }
}