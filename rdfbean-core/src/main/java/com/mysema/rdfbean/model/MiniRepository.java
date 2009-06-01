/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;
import org.apache.commons.collections15.iterators.IteratorChain;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author sasa
 *
 */
public final class MiniRepository implements Repository<MiniDialect> {
    
    private Map<ID, Map<UID, Set<STMT>>> subjects = new HashMap<ID, Map<UID, Set<STMT>>>();
    
    private Map<ID, Map<UID, Set<STMT>>> objects = new HashMap<ID, Map<UID, Set<STMT>>>();
    
    private static final Iterator<STMT> EMPTY_ITERATOR = new Iterator<STMT>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public STMT next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
        }
        
    };
    
    private Set<STMT> statements = new LinkedHashSet<STMT>();
    
    private MiniDialect dialect;
    
    public MiniRepository() {
        dialect = new MiniDialect();
    }

    public MiniRepository(STMT... stmts) {
        this();
        add(stmts);
    }
    
    public void add(STMT... stmts) {
        for (STMT stmt : stmts) {
            statements.add(stmt);
            index(stmt.getSubject(), stmt, subjects);
            if (stmt.getObject().isResource()) {
                index((ID) stmt.getObject(), stmt, objects);
            }
        }
    }
    
    public void index(ID key, STMT stmt, Map<ID, Map<UID, Set<STMT>>> map) {
        Map<UID, Set<STMT>> stmtMap = map.get(key);
        if (stmtMap == null) {
            stmtMap = new HashMap<UID, Set<STMT>>();
            map.put(key, stmtMap);
        } 
        Set<STMT> stmts = stmtMap.get(stmt.getPredicate());
        if (stmts == null) {
            stmts = new LinkedHashSet<STMT>();
            stmtMap.put(stmt.getPredicate(), stmts);
        }
        stmts.add(stmt);
    }

    public CloseableIterator<STMT> findStatements(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        Iterator<STMT> iterator = null;
        boolean optimized = false;
        if (subject != null) {
            optimized = true;
            iterator = getIndexed(subject, predicate, subjects);
        } else if (object != null && object.isResource()) {
            optimized = true;
            iterator = getIndexed((ID) object, predicate, objects);
        }
        if (!optimized) {
            iterator = statements.iterator();
        }
        return new ResultIterator(iterator, subject, predicate, object, context, includeInferred);
    }
    
    private Iterator<STMT> getIndexed(ID key, UID predicate, Map<ID, Map<UID, Set<STMT>>> index) {
        Iterator<STMT> iterator;
        Map<UID, Set<STMT>> stmtMap = index.get(key);
        if (stmtMap == null) {
            iterator = EMPTY_ITERATOR;
        } else if (predicate != null) {
            if (stmtMap.containsKey(predicate)) {
                iterator = stmtMap.get(predicate).iterator();
            } else {
                iterator = EMPTY_ITERATOR;
            }
        } else {
            IteratorChain<STMT> iterChain = new IteratorChain<STMT>();
            for (Set<STMT> stmts : stmtMap.values()) {
                iterChain.addIterator(stmts.iterator());
            }
            iterator = iterChain;
        }
        return iterator;
    }
    
    public static class ResultIterator implements CloseableIterator<STMT> {
        
        private Iterator<STMT> iter;
        
        private ResultIterator(Iterable<STMT> iterable, final ID subject, final UID predicate, 
                final NODE object, final UID context, final boolean includeInferred) {
            this(iterable.iterator(), subject, predicate, object, context, includeInferred);
        }
        
        private ResultIterator(Iterator<STMT> iterator, final ID subject, final UID predicate, 
                final NODE object, final UID context, final boolean includeInferred) {
            this.iter = new FilterIterator<STMT>(iterator, new Predicate<STMT>() {

                @Override
                public boolean evaluate(STMT stmt) {
                    return 
                    // Subject match
                    (subject == null || stmt.getSubject().equals(subject)) &&

                    // Predicate match
                    (predicate == null || predicate.equals(stmt.getPredicate())) &&
                    
                    // Object match
                    (object == null || object.equals(stmt.getObject())) &&
                    
                    // Context match
                    (context == null || context.equals(stmt.getContext())) &&
                    
                    // Asserted or includeInferred statement
                    (includeInferred || stmt.isAsserted());
                }
                
            });
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public STMT next() {
            return iter.next();
        }

        @Override
        public void remove() {
        }

        @Override
        public void close() throws IOException {
        }
        
    }
    
    public MiniDialect getDialect() {
        return dialect;
    }

    public void removeStatement(STMT... stmts) {
        for (STMT stmt : stmts) {
            if (statements.remove(stmt)) {
                removeIndexed(stmt.getSubject(), stmt, subjects);
                if (stmt.getObject().isResource()) {
                    removeIndexed((ID) stmt.getObject(), stmt, objects);
                }
            }
        }
    }
    
    private void removeIndexed(ID key, STMT stmt, Map<ID, Map<UID, Set<STMT>>> index) {
        Map<UID, Set<STMT>> stmtMap = index.get(key);
        if (stmtMap != null) {
            Set<STMT> stmts = stmtMap.get(stmt.getPredicate());
            if (stmts != null) {
                stmts.remove(stmt);
            }
        }
    }

    public MiniConnection openConnection() {
        return new MiniConnection(this);
    }
    
    public void addStatements(CloseableIterator<STMT> stmts) {
        try {
            while (stmts.hasNext()) {
                add(stmts.next());
            }
        } finally {
            try {
                stmts.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
