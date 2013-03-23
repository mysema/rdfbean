/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Collection;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;

/**
 * MultiConnection provides an implementation of the RDFConnection interface to
 * populate multiple backends via a single RDFConnection handle
 * 
 * @author tiwe
 * @version $Id$
 */
public class MultiConnection implements RDFConnection {

    private final RDFConnection[] connections;

    @Nullable
    private RDFBeanTransaction localTxn;

    private boolean readonlyTnx;

    public MultiConnection(RDFConnection... connections) {
        this.connections = Assert.notNull(connections, "connections");
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout,
            int isolationLevel) {
        RDFBeanTransaction[] transactions = new RDFBeanTransaction[connections.length];
        for (int i = 0; i < transactions.length; i++) {
            transactions[i] = connections[i].beginTransaction(readOnly, txTimeout, isolationLevel);
        }
        this.readonlyTnx = readOnly;
        localTxn = new MultiTransaction(this, transactions);
        return localTxn;
    }

    public void cleanUpAfterCommit() {
        localTxn = null;
        readonlyTnx = false;
    }

    public void cleanUpAfterRollback() {
        localTxn = null;
        readonlyTnx = false;
        close();
    }

    @Override
    public void clear() {
        for (RDFConnection connection : connections) {
            connection.clear();
        }
    }

    @Override
    public void close() {
        for (RDFConnection connection : connections) {
            connection.close();
        }
    }

    @Override
    public BID createBNode() {
        return connections[0].createBNode();
    }

    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        return connections[0].createQuery(queryLanguage, definition);
    }

    @Override
    public <D, Q> Q createUpdate(UpdateLanguage<D, Q> updateLanguage, D definition) {
        return connections[0].createUpdate(updateLanguage, definition);
    }

    @Override
    public boolean exists(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        return connections[0].exists(subject, predicate, object, context, includeInferred);
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        return connections[0].findStatements(subject, predicate, object, context, includeInferred);
    }

    @Override
    public InferenceOptions getInferenceOptions() {
        return connections[0].getInferenceOptions();
    }

    @Override
    public long getNextLocalId() {
        return connections[0].getNextLocalId();
    }

    @Override
    public QueryOptions getQueryOptions() {
        return connections[0].getQueryOptions();
    }

    @Override
    public void remove(ID subject, UID predicate, NODE object, UID context) {
        connections[0].remove(subject, predicate, object, context);
    }

    @Override
    public void update(Collection<STMT> removedStatements, Collection<STMT> addedStatements) {
        if (!readonlyTnx) {
            for (RDFConnection connection : connections) {
                connection.update(removedStatements, addedStatements);
            }
        }
    }

}
