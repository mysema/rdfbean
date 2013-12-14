/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.virtuoso;

import java.sql.Connection;
import java.sql.SQLException;

import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * @author tiwe
 * 
 */
public class VirtuosoTransaction implements RDFBeanTransaction {

    private final Connection conn;

    private boolean rollbackOnly = false;

    public VirtuosoTransaction(Connection connection, boolean readOnly, int txTimeout, int isolationLevel) {
        this.conn = connection;
        try {
            conn.setAutoCommit(false);
            conn.setReadOnly(readOnly);
            if (isolationLevel != -1) {
                connection.setTransactionIsolation(isolationLevel);
            } else {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void commit() {
        try {
            if (!rollbackOnly) {
                conn.commit();
            } else {
                throw new RepositoryException("Tx was set to rollbackOnly");
            }
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isActive() {
        try {
            return !conn.isClosed();
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

    }

    @Override
    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;

    }

}
