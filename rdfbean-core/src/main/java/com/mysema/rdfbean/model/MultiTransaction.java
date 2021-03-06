/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.commons.lang.Assert;

/**
 * MultiTransaction is the RDFBeanTransaction of MultiConnection
 * 
 * @author tiwe
 * @version $Id$
 */
public class MultiTransaction implements RDFBeanTransaction {

    private boolean rollbackOnly = false;

    private final MultiConnection connection;

    private final RDFBeanTransaction[] transactions;

    public MultiTransaction(MultiConnection connection, RDFBeanTransaction[] transactions) {
        this.connection = Assert.notNull(connection, "connection");
        this.transactions = Assert.notNull(transactions, "transactions");
        if (transactions.length == 0) {
            throw new IllegalArgumentException("No transactions given");
        }
    }

    @Override
    public void commit() {
        if (rollbackOnly) {
            throw new RepositoryException("Transaction is rollBackOnly");
        }
        try {
            prepare();
        } catch (RuntimeException e) {
            rollback();
            throw new RepositoryException(e);
        }

        try {
            for (RDFBeanTransaction tx : transactions) {
                tx.commit();
            }
        } finally {
            connection.cleanUpAfterCommit();
        }

    }

    @Override
    public boolean isActive() {
        return transactions[0].isActive();
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    @Override
    public void rollback() {
        try {
            for (RDFBeanTransaction tx : transactions) {
                tx.rollback();
            }
        } finally {
            connection.cleanUpAfterCommit();
        }

    }

    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
        for (RDFBeanTransaction tx : transactions) {
            tx.setRollbackOnly();
        }
    }

    @Override
    public void prepare() {
        for (RDFBeanTransaction tx : transactions) {
            tx.prepare();
        }
    }

}
