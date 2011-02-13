/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Collection;

import com.mysema.commons.lang.CloseableIterator;

/**
 * MiniConnection is an RDFConnection implementation for the MiniRepository
 * 
 * @author sasa
 *
 */
public class MiniConnection implements RDFConnection {
    
    private final MiniRepository repository;

    public MiniConnection(MiniRepository repository) {
        this.repository = repository;
    }

    public void addStatements(STMT... stmts) {
        this.repository.add(stmts);
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
    }
    
    @Override
    public void close() {
    }

    @Override
    public BID createBNode() {
        return new BID();
    }

    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        return repository.exists(subject, predicate, object, context);
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        return repository.findStatements(subject, predicate, object, context, includeInferred);
    }
    
    @Override
    public long getNextLocalId() {
        return repository.getNextLocalId();
    }

    public MiniRepository getRepository() {
        return repository;
    }

    @Override
    public void remove(ID subject, UID predicate, NODE object, UID context) {
        repository.remove(subject, predicate, object, context);
    }

    @Override
    public void update(Collection<STMT> removedStatements, Collection<STMT> addedStatements) {
        if (removedStatements != null) {
            repository.removeStatements(removedStatements);
        }
        if (addedStatements != null) {
            repository.add(addedStatements.toArray(new STMT[addedStatements.size()]));
        }
    }

    @Override
    public QueryOptions getQueryOptions() {
        return QueryOptions.DEFAULT;
    }

    @Override
    public InferenceOptions getInferenceOptions() {
        return InferenceOptions.FULL;
    }
}
