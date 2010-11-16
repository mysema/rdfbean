package com.mysema.rdfbean.sesame;

import org.openrdf.model.Statement;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public abstract class AbstractResultIterator implements CloseableIterator<STMT>{

    private final SesameDialect dialect;
    
    public AbstractResultIterator(SesameDialect dialect) {
        this.dialect = dialect;
    }
    
    protected STMT convert(Statement statement, boolean asserted) {
        UID context = statement.getContext() != null ? (UID)dialect.getID(statement.getContext()) : null;
        return new STMT(
                dialect.getID(statement.getSubject()), 
                dialect.getUID(statement.getPredicate()), 
                dialect.getNODE(statement.getObject()), 
                context, asserted);
    }
    
}
