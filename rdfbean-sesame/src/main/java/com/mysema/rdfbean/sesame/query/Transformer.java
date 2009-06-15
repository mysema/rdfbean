/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.List;

import org.openrdf.query.algebra.ValueExpr;

/**
 * Transformer provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface Transformer{

    ValueExpr transform(List<ValueExpr> args);
    
}