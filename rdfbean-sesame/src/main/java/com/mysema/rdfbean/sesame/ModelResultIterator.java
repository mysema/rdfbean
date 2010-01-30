/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;

import org.openrdf.model.Statement;
import org.openrdf.result.ModelResult;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * ModelResultIterator provides a CloseableIterator adapter for ModelResults
 *
 * @author tiwe
 * @author sasa
 * @version $Id$
 */
public class ModelResultIterator implements CloseableIterator<STMT>{
    
    private final SesameDialect dialect;
    
    private final ModelResult statements;
    
    private final boolean includeInferred;
    
    public ModelResultIterator(SesameDialect dialect, ModelResult statements, boolean includeInferred){
        this.dialect = Assert.notNull(dialect);
        this.statements = Assert.notNull(statements);
        this.includeInferred = includeInferred;
    }
    

    @Override
    public void close() throws IOException {
        try {
            statements.close();
        } catch (StoreException e1) {
            throw new IOException(e1);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return statements.hasNext();
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public STMT next() {
        try {
            return convert(statements.next(), !includeInferred);
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
    
    private STMT convert(Statement statement, boolean asserted) {
        UID context = statement.getContext() != null ? (UID)dialect.getID(statement.getContext()) : null;
        return new STMT(
                dialect.getID(statement.getSubject()), 
                dialect.getUID(statement.getPredicate()), 
                dialect.getNODE(statement.getObject()), 
                context, asserted);
    }

}
