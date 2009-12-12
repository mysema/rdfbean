/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.Set;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SimpleBeanQuery;

/**
 * @author sasa
 *
 */
public class MiniConnection implements RDFConnection {
    
    private final MiniRepository repository;

    public MiniConnection(MiniRepository repository) {
        this.repository = repository;
    }

    @Override
    public BID createBNode() {
        return new BID();
    }

    @Override
    public BeanQuery createQuery(Session session) {
        return new SimpleBeanQuery(session);
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        return repository.findStatements(subject, predicate, object, context, includeInferred);
    }

    public void addStatements(CloseableIterator<STMT> stmts) {
        this.repository.addStatements(stmts);
    }
    
    public void addStatements(STMT... stmts) {
        this.repository.add(stmts);
    }

    @Override
    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {
        if (removedStatements != null) {
            repository.removeStatement(removedStatements.toArray(new STMT[removedStatements.size()]));
        }
        if (addedStatements != null) {
            repository.add(addedStatements.toArray(new STMT[addedStatements.size()]));
        }
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly,
            int txTimeout, int isolationLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
    }

    public MiniRepository getRepository() {
        return repository;
    }

    @Override
    public <Q> Q createQuery(Session session, Class<Q> queryType) {
        throw new UnsupportedOperationException();
    }

}
