package com.mysema.rdfbean.rdb;

import java.sql.Connection;
import java.sql.SQLException;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * RDBTransaction provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBTransaction implements RDFBeanTransaction{

    private final Connection connection;
    
    private boolean rollbackOnly;
    
    public RDBTransaction(Connection connection) {
        this.connection = Assert.notNull(connection,"connection");
    }

    @Override
    public void commit() {
        if (rollbackOnly){
            throw new RepositoryException("Transaction is rollBackOnly");
        }        
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isActive() {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    @Override
    public void prepare() {
        // TODO        
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }        
    }

    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;
        
    }

}
