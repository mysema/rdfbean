/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
    
    private Map<ID, STMTCache> subjects = new HashMap<ID, STMTCache>();
    
    private Map<ID, STMTCache> objects = new HashMap<ID, STMTCache>();
    
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
            index(stmt.getSubject(), stmt, subjects);
            if (stmt.getObject().isResource()) {
                index((ID) stmt.getObject(), stmt, objects);
            }
        }
    }
    
    public void index(ID key, STMT stmt, Map<ID, STMTCache> index) {
        STMTCache stmtCache = index.get(key);
        if (stmtCache == null) {
            stmtCache = new STMTCache();
            index.put(key, stmtCache);
        } 
        stmtCache.add(stmt);
    }

    public CloseableIterator<STMT> findStatements(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        Iterator<STMT> iterator = null;
        if (subject != null) {
            iterator = getIndexed(subject, predicate, subjects);
        } else if (object != null && object.isResource()) {
            iterator = getIndexed((ID) object, predicate, objects);
        } else {
            IteratorChain<STMT> iterChain = new IteratorChain<STMT>();
            for (STMTCache stmtCache : subjects.values()) {
                iterChain.addIterator(stmtCache.iterator(predicate));
            }
            iterator = iterChain;
        }
        return new ResultIterator(iterator, subject, predicate, object, context, includeInferred);
    }
    
    private Iterator<STMT> getIndexed(ID key, UID predicate, Map<ID, STMTCache> index) {
        STMTCache stmtCache = index.get(key);
        if (stmtCache != null) {
            return stmtCache.iterator(predicate);
        } else {
            return EMPTY_ITERATOR;
        }
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
            if (removeIndexed(stmt.getSubject(), stmt, subjects)) {
                if (stmt.getObject().isResource()) {
                    removeIndexed((ID) stmt.getObject(), stmt, objects);
                }
            }
        }
    }
    
    private boolean removeIndexed(ID key, STMT stmt, Map<ID, STMTCache> index) {
        STMTCache stmtMap = index.get(key);
        if (stmtMap != null) {
            return stmtMap.remove(stmt);
        } else {
            return false;
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
    
    public static class STMTCache {
    
        private Map<UID, Set<STMT>> predicates;
        
        private List<STMT> containerProperties;
        
        public Iterator<STMT> iterator(UID predicate) {
            if (predicate == null) {
                IteratorChain<STMT> iterChain = new IteratorChain<STMT>();
                if (predicates != null) {
                    for (Set<STMT> stmts : predicates.values()) {
                        iterChain.addIterator(stmts.iterator());
                    }
                }
                if (containerProperties != null) {
                    iterChain.addIterator(containerProperties.iterator());
                }
                return iterChain;
            } else if (RDF.isContainerMembershipProperty(predicate)) {
                if (containerProperties != null) {
                    return containerProperties.iterator();
                } 
            } else {
                Set<STMT> stmts  = predicates.get(predicate);
                if (stmts != null) {
                    return stmts.iterator();
                }
            }
            return EMPTY_ITERATOR;
        }
        
        public void add(STMT stmt) {
            if (RDF.isContainerMembershipProperty(stmt.getPredicate())) {
                if (containerProperties == null) {
                    containerProperties = new ArrayList<STMT>();
                }
                containerProperties.add(stmt);
            } else {
                if (predicates == null) {
                    predicates = new LinkedHashMap<UID, Set<STMT>>();
                }
                Set<STMT> stmts = predicates.get(stmt.getPredicate());
                if (stmts == null) {
                    stmts = new LinkedHashSet<STMT>();
                    predicates.put(stmt.getPredicate(), stmts);
                }
                stmts.add(stmt);
            }
        }
        
        public boolean remove(STMT stmt) {
            if (RDF.isContainerMembershipProperty(stmt.getPredicate())) {
                if (containerProperties != null) {
                    return containerProperties.remove(stmt);
                }
            } else {
                if (predicates != null) {
                    Set<STMT> stmts = predicates.get(stmt.getPredicate());
                    if (stmts != null) {
                        return stmts.remove(stmt);
                    }
                }
            }
            return false;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (predicates != null) {
                sb.append(predicates.toString());
            } 
            if (containerProperties != null) {
                sb.append(containerProperties.toString());
            }
            return sb.toString();
        }
    }
}
