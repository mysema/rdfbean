/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryMetadata;

/**
 * MiniConnection is an RDFConnection implementation for the MiniRepository
 * 
 * @author sasa
 * 
 */
public class MiniConnection implements RDFConnection {

    private static final Logger logger = LoggerFactory.getLogger(MiniConnection.class);

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
    public <D, Q> Q createUpdate(UpdateLanguage<D, Q> updateLanguage, D definition) {
        throw new UnsupportedOperationException(updateLanguage.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        if (queryLanguage == QueryLanguage.TUPLE
                || queryLanguage == QueryLanguage.GRAPH
                || queryLanguage == QueryLanguage.BOOLEAN) {
            if (logger.isDebugEnabled()) {
                QueryMetadata metadata = (QueryMetadata) definition;
                logger.debug(queryLanguage + " : " + metadata.getWhere().toString());
            }
            QueryRDFVisitor visitor = new QueryRDFVisitor(this);
            return (Q) visitor.visit((QueryMetadata) definition, queryLanguage);

        } else {
            throw new UnsupportedOperationException(queryLanguage.toString());
        }
    }

    @Override
    public boolean exists(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        if (logger.isDebugEnabled()) {
            logger.debug("exists " + subject + " " + predicate + " " + object + " " + context);
        }
        return repository.exists(subject, predicate, object, context);
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        if (logger.isDebugEnabled()) {
            logger.debug("find " + subject + " " + predicate + " " + object + " " + context);
        }
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
        return QueryOptions.PRESERVE_STRING_OPS;
    }

    @Override
    public InferenceOptions getInferenceOptions() {
        return InferenceOptions.DEFAULT;
    }

}
