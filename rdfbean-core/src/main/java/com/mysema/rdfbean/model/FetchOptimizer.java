/**
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
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;

/**
 * @author sasa
 *
 */
public class FetchOptimizer implements RDFConnection {
    
    static final int DEFAULT_INITIAL_CAPACITY = 1024;

    private RDFConnection connection;
    
    private MiniConnection cache = new MiniRepository(DEFAULT_INITIAL_CAPACITY).openConnection();
    
    private Set<STMTMatcher> cacheKeys = new HashSet<STMTMatcher>(DEFAULT_INITIAL_CAPACITY);
    
    private List<FetchStrategy> fetchStrategies = new ArrayList<FetchStrategy>();

    public FetchOptimizer() {}
    
    public FetchOptimizer(RDFConnection connection) {
        this(connection, new ArrayList<FetchStrategy>());
    }
    
    public FetchOptimizer(RDFConnection connection, FetchStrategy fetchStrategy) {
        this(connection, Arrays.asList(fetchStrategy));
    }
    
    public FetchOptimizer(RDFConnection connection, List<FetchStrategy> fetchStrategies) {
        this.connection = Assert.notNull(connection);
        this.fetchStrategies = Assert.notNull(fetchStrategies);
    }

    public RDFBeanTransaction beginTransaction(Session session,
            boolean readOnly, int txTimeout, int isolationLevel) {
        return connection.beginTransaction(session, readOnly, txTimeout,
                isolationLevel);
    }

    public void close() throws IOException {
        connection.close();
    }

    public BID createBNode() {
        return connection.createBNode();
    }

    public BeanQuery createQuery(Session session) {
        return connection.createQuery(session);
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
            try {
                if (stmts != null) {
                    stmts.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return cache.findStatements(subject, predicate, object, context, includeInferred);
//        return new IteratorWrapper<STMT>(result.iterator());
    }
    
    public MiniRepository getCache() {
        return cache.getRepository();
    }

    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {
        cache.update(removedStatements, addedStatements);
        connection.update(removedStatements, addedStatements);
    }

    @Override
    public void clear() {
        cache = new MiniRepository(DEFAULT_INITIAL_CAPACITY).openConnection();
        cacheKeys = new HashSet<STMTMatcher>(DEFAULT_INITIAL_CAPACITY);
        connection.clear();
    }

    public void setConnection(RDFConnection connection) {
        this.connection = connection;
    }

    public void setFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }
    
    public void addFetchStrategies(List<FetchStrategy> fetchStrategies) {
        this.fetchStrategies.addAll(fetchStrategies);
    }
    
}
