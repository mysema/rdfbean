/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query.functions;

import org.openrdf.query.algebra.evaluation.function.Function;

import com.mysema.query.types.operation.Operator;
import com.mysema.rdfbean.model.UID;

/**
 * BaseFunction provides
 * 
 * @author tiwe
 * @version $Id$
 * 
 */
abstract class BaseFunction implements Function {
    private final String uri;

    private final Operator<?>[] ops;

    public BaseFunction(UID uid, Operator<?>... ops) {
        this.uri = uid.getId();
        this.ops = ops;
    }

    @Override
    public final String getURI() {
        return uri;
    }

    public final Operator<?>[] getOps() {
        return ops;
    }
}