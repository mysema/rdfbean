/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.springframework.util.Assert;

/**
 * @author sasa
 *
 */
public class StatementAccessor {

    private Statement statement;
    
    private boolean inverse;
    
    // TODO asserted/inferred status?
    
    public StatementAccessor(Statement statement, boolean inverse) {
        Assert.notNull(statement);
        this.statement = statement;
        this.inverse = inverse;
    }

    public Value getValue() {
        return inverse ? statement.getSubject() : statement.getObject();
    }
}
