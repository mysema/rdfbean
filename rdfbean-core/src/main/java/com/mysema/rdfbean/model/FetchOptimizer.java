/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.object.Session;

/**
 * FetchOptimizer is an adapter implementation of the RDFConnection to provide
 * fetch optimized queries on top of RDFConnection instances.
 * 
 * @author sasa
 *
 */
public class FetchOptimizer implements RDFConnection {
    
    static final int DEFAULT_INITIAL_CAPACITY = 1024;

    private MiniConnection cache;
    
    private Set<STMTMatcher> cacheKeys = new HashSet<STMTMatcher>(DEFAULT_INITIAL_CAPACITY);
    
    private final RDFConnection connection;
    
    private List<FetchStrategy> fetchStrategies = new ArrayList<FetchStrategy>();
    
    private final boolean inverseCache;

    public FetchOptimizer(RDFConnection connection) {
        this(connection, new ArrayList<FetchStrategy>());
    }
    
    public FetchOptimizer(RDFConnection connection, FetchStrategy fetchStrategy) {
        this(connection, fetchStrategy, true);
    }
    
    public FetchOptimizer(RDFConnection connection, FetchStrategy fetchStrategy, boolean inverseCache) {
        this(connection, Arrays.asList(fetchStrategy), inverseCache);
    }
    
    public FetchOptimizer(RDFConnection connection, List<FetchStrategy> fetchStrategies) {
        this(connection, fetchStrategies, true);
    }
    
    public FetchOptimizer(RDFConnection connection, List<FetchStrategy> fetchStrategies, boolean inverseCache) {
        this.connection = Assert.notNull(connection,"connection");
        this.fetchStrategies = Assert.notNull(fetchStrategies,"fetchStrategies");
        this.inverseCache = inverseCache;
        cache = new MiniRepository(DEFAULT_INITIAL_CAPACITY, this.inverseCache).openConnection();
    }

    public void addFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies.addAll(fetchStrategies);
    }

    public RDFBeanTransaction beginTransaction(boolean readOnly,
            int txTimeout, int isolationLevel) {
        return connection.beginTransaction(readOnly, txTimeout, isolationLevel);
    }

    private CloseableIterator<STMT> cacheStatements(CloseableIterator<STMT> stmts, ID subject, UID predicate, NODE object,
            UID context, boolean includeInferred) {
//        List<STMT> result = new ArrayList<STMT>();
        try {
            while (stmts.hasNext()) {
                cache.addStatements(stmts.next());
//               STMT stmt = stmts.next();
//               if (STMTMatcher.matches(stmt, subject, predicate, object, context, includeInferred)) {
//                   result.add(stmt);
//               }
//               cache.addStatements(stmt);
            }
        } finally {
            if (stmts != null) {
                stmts.close();
            }
        }
        return cache.findStatements(subject, predicate, object, context, includeInferred);
//        return new IteratorWrapper<STMT>(result.iterator());
    }

    @Override
    public void clear() {
        cache = new MiniRepository(DEFAULT_INITIAL_CAPACITY, this.inverseCache).openConnection();
        cacheKeys = new HashSet<STMTMatcher>(DEFAULT_INITIAL_CAPACITY);
        connection.clear();
    }

    public void close() throws IOException {
        connection.close();
    }
            
    public BID createBNode() {
        return connection.createBNode();
    }
    
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        boolean cached = false;
        for (FetchStrategy fetchStrategy : fetchStrategies) {
            STMTMatcher matcher = fetchStrategy.getCacheKey(subject, predicate, object, context, includeInferred);
            if (matcher != null) {
                cached = true;
                if (cacheKeys.add(matcher)) {
//                    System.out.print("A");
                    return cacheStatements(connection.findStatements(
                            matcher.getSubject(),
                            matcher.getPredicate(),
                            matcher.getObject(), 
                            matcher.getContext(), 
                            matcher.isIncludeInferred()),
                            subject, predicate, object, context, includeInferred);
                }
            }
        }
        if (!cached && !includeInferred) {
            cached = true;
//            System.out.print("A");
            return cacheStatements(connection.findStatements(subject, predicate, object, context, includeInferred),
                    subject, predicate, object, context, includeInferred);
        }
        if (cached) {
//            System.out.print("C");
            return cache.findStatements(subject, predicate, object, context, includeInferred);
        } else {
//            System.out.print("-");
            return connection.findStatements(subject, predicate, object, context, includeInferred);
        }
    }

    public MiniRepository getCache() {
        return cache.getRepository();
    }

    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        return connection.createQuery(queryLanguage, definition);
    }
    
    @Override
    public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
        return connection.createQuery(session, queryLanguage, definition);
    }
    
    public void setFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }

    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {
        cache.update(removedStatements, addedStatements);
        connection.update(removedStatements, addedStatements);
    }

    @Override
    public long getNextLocalId() {
        return connection.getNextLocalId();
    }

    
}
