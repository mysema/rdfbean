package com.mysema.rdfbean.model;

import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;

/**
 * @author tiwe
 * 
 */
public interface Block extends Predicate {

    BooleanExpression exists();

}
