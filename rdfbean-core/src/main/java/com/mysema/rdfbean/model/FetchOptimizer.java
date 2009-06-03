/**
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.ArrayList;
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

    private RDFConnection connection;
    
    private MiniConnection cache = new MiniRepository().openConnection();
    
    private Set<Object> cacheKeys = new HashSet<Object>();
    
    private List<FetchStrategy> fetchStrategies = new ArrayList<FetchStrategy>();

    public FetchOptimizer() {}
    
    public FetchOptimizer(RDFConnection connection) {
        this(connection, new ArrayList<FetchStrategy>());
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
            Object cacheKey = fetchStrategy.getCacheKey(subject, predicate, object, context, includeInferred);
            if (cacheKey != null) {
                cached = true;
                if (cacheKeys.add(cacheKey)) {
                    cache.addStatements(fetchStrategy.fetchStatements(connection, subject, predicate, object, context, includeInferred));
                }
                break;
            }
        }
        if (!cached && !includeInferred) {
            cached = true;
            cache.addStatements(connection.findStatements(subject, predicate, object, context, includeInferred));
        }
        if (cached) {
            return cache.findStatements(subject, predicate, object, context, includeInferred);
        } else {
          return connection.findStatements(subject, predicate, object, context,
          includeInferred);
        }
    }

    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {
        cache.update(removedStatements, addedStatements);
        connection.update(removedStatements, addedStatements);
    }

    @Override
    public void clear() {
        cache = new MiniRepository().openConnection();
        cacheKeys = new HashSet<Object>();
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
