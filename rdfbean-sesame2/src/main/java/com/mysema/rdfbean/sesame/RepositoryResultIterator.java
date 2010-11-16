/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryResult;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;

/**
 * RepositoryResultIterator provides a CloseableIterator adapter for ModelResults
 *
 * @author tiwe
 * @author sasa
 * @version $Id$
 */
public class RepositoryResultIterator extends AbstractResultIterator{
    
    private final RepositoryResult<Statement> statements;
    
    private final boolean includeInferred;
    
    public RepositoryResultIterator(SesameDialect dialect, RepositoryResult<Statement> statements, boolean includeInferred){
        super(dialect);
        this.statements = Assert.notNull(statements,"statements");
        this.includeInferred = includeInferred;
    }
    

    @Override
    public void close(){
        try {
            statements.close();
        } catch (org.openrdf.repository.RepositoryException e1) {
            throw new RepositoryException(e1);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return statements.hasNext();
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public STMT next() {
        try {
            return convert(statements.next(), !includeInferred);
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
    
}
