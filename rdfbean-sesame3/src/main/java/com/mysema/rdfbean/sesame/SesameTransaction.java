/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.openrdf.store.Isolation;
import org.openrdf.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * SesameTransaction provides an RDFBeanTransaction implementation for
 * SesameConnection
 * 
 * @author tiwe
 * @version $Id$
 */
public class SesameTransaction implements RDFBeanTransaction {

    private static final Map<Integer, Isolation> isolationLevels;

    private static final Logger logger = LoggerFactory.getLogger(SesameTransaction.class);

    static {
        Map<Integer, Isolation> levels = new HashMap<Integer, Isolation>();
        levels.put(Connection.TRANSACTION_READ_COMMITTED, Isolation.READ_COMMITTED);
        levels.put(Connection.TRANSACTION_READ_UNCOMMITTED, Isolation.READ_UNCOMMITTED);
        levels.put(Connection.TRANSACTION_REPEATABLE_READ, Isolation.REPEATABLE_READ);
        levels.put(Connection.TRANSACTION_SERIALIZABLE, Isolation.SERIALIZABLE);
        isolationLevels = Collections.unmodifiableMap(levels);
    }

    private boolean active = false;

    private final SesameConnection connection;

    @Nullable
    private Isolation isolationLevel;

    private boolean rollbackOnly;

    public SesameTransaction(SesameConnection connection, int isolationLevel) {
        this.connection = Assert.notNull(connection, "connection");
        this.isolationLevel = isolationLevels.containsKey(isolationLevel)
                ? isolationLevels.get(isolationLevel) : null;
    }

    public void begin() {
        try {
            connection.getConnection().setTransactionIsolation(isolationLevel);
            connection.getConnection().begin();
        } catch (StoreException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }
        active = true;
    }

    @Override
    public void commit() {
        if (rollbackOnly) {
            throw new RepositoryException("Transaction is rollBackOnly");
        }
        try {
            connection.getConnection().commit();
        } catch (StoreException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        } finally {
            connection.cleanUpAfterCommit();
        }

    }

    public boolean isActive() {
        return active;
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
            connection.getConnection().rollback();
        } catch (StoreException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        } finally {
            connection.cleanUpAfterRollback();
        }
    }

    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;

    }

}
