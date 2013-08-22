/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.result.ModelResult;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;

/**
 * ModelResultIterator provides a CloseableIterator adapter for ModelResults
 * 
 * @author tiwe
 * @author sasa
 * @version $Id$
 */
public class ModelResultIterator extends AbstractResultIterator {

    private final ModelResult statements;

    private final boolean includeInferred;

    public ModelResultIterator(SesameDialect dialect, ModelResult statements, boolean includeInferred) {
        super(dialect);
        this.statements = Assert.notNull(statements, "statements");
        this.includeInferred = includeInferred;
    }

    @Override
    public void close() {
        try {
            statements.close();
        } catch (StoreException e1) {
            throw new RepositoryException(e1);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return statements.hasNext();
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public STMT next() {
        try {
            return convert(statements.next(), !includeInferred);
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
