/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.Collection;

import javax.annotation.Nullable;

import org.openrdf.query.algebra.ValueExpr;

import com.mysema.query.types.operation.Operation;
import com.mysema.query.types.operation.Operator;

/**
 * Transformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface Transformer{

    @Nullable
    ValueExpr transform(Operation<?,?> operation, TransformerContext context);

    Collection<? extends Operator<?>> getSupportedOperations();
    
}