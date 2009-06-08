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
import org.apache.commons.collections15.iterators.SingletonIterator;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author sasa
 *
 */
public final class MiniRepository implements Repository<MiniDialect> {
    
    private Map<ID, PredicateCache> subjects;
    
    private Map<ID, PredicateCache> objects;
    
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
    
    private MiniDialect dialect = new MiniDialect();
    
    public MiniRepository() {
        this(1024);
    }

    public MiniRepository(STMT... stmts) {
        this(stmts.length);
        add(stmts);
    }
    
    public MiniRepository(int initialCapacity) {
        objects = new HashMap<ID, PredicateCache>(initialCapacity);
        subjects = new HashMap<ID, PredicateCache>(initialCapacity);
    }
    
    public void add(STMT... stmts) {
        for (STMT stmt : stmts) {
            index(stmt.getSubject(), stmt, subjects);
            if (stmt.getObject().isResource()) {
                index((ID) stmt.getObject(), stmt, objects);
            }
        }
    }
    
    public void index(ID key, STMT stmt, Map<ID, PredicateCache> index) {
        PredicateCache stmtCache = index.get(key);
        if (stmtCache == null) {
            stmtCache = new PredicateCache();
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
            for (PredicateCache stmtCache : subjects.values()) {
                iterChain.addIterator(stmtCache.iterator(predicate));
            }
            iterator = iterChain;
        }
        return new ResultIterator(iterator, subject, predicate, object, context, includeInferred);
    }
    
    private Iterator<STMT> getIndexed(ID key, UID predicate, Map<ID, PredicateCache> index) {
        PredicateCache stmtCache = index.get(key);
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
                    return STMTMatcher.matches(stmt, subject, predicate, object, context, includeInferred);
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
    
    private boolean removeIndexed(ID key, STMT stmt, Map<ID, PredicateCache> index) {
        PredicateCache stmtMap = index.get(key);
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
        
        private STMT single;
        
        private Set<STMT> multi;
        
        public STMTCache(STMT single) {
            this.single = single;
        }
        
        public void add(STMT stmt) {
            if (multi == null) {
                if (single == null) {
                    single = stmt;
                } else if (!stmt.equals(single)) {
                    multi = new LinkedHashSet<STMT>();
                    multi.add(single);
                    multi.add(stmt);
                }
            } else {
                multi.add(stmt);
            }
        }
        
        public boolean remove(STMT stmt) {
            if (multi == null) {
                if (stmt.equals(single)) {
                    single = null;
                    return true;
                } else {
                    return false;
                }
            } else {
                return multi.remove(stmt);
            }
        }
        
        public Iterator<STMT> iterator() {
            if (multi == null) {
                if (single == null) {
                    return EMPTY_ITERATOR;
                } else {
                    return new SingletonIterator<STMT>(single);
                }
            } else {
                return multi.iterator();
            }
        }
    }
    
    public static class PredicateCache {
    
        private Map<UID, STMTCache> predicates;
        
        private List<STMT> containerProperties;
        
        public Iterator<STMT> iterator(UID predicate) {
            if (predicate == null) {
                IteratorChain<STMT> iterChain = new IteratorChain<STMT>();
                if (predicates != null) {
                    for (STMTCache stmts : predicates.values()) {
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
                STMTCache stmts  = predicates.get(predicate);
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
                    predicates = new LinkedHashMap<UID, STMTCache>();
                }
                STMTCache stmts = predicates.get(stmt.getPredicate());
                if (stmts == null) {
                    stmts = new STMTCache(stmt);
                    predicates.put(stmt.getPredicate(), stmts);
                } else {
                    stmts.add(stmt);
                }
            }
        }
        
        public boolean remove(STMT stmt) {
            if (RDF.isContainerMembershipProperty(stmt.getPredicate())) {
                if (containerProperties != null) {
                    return containerProperties.remove(stmt);
                }
            } else {
                if (predicates != null) {
                    STMTCache stmts = predicates.get(stmt.getPredicate());
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
