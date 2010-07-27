/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections15.iterators.IteratorChain;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFWriter;

/**
 * MiniRepository is a lightweight implementation of the Repository interface
 * for use in local cacheing of statements and tests
 * 
 * @author sasa
 *
 */
public final class MiniRepository implements Repository{
    
    private MiniDialect dialect = new MiniDialect();
    
    private long localId = 0;
    
    @Nullable
    private final Map<ID, PredicateCache> objects;
    
    private final Map<ID, PredicateCache> subjects;
    
    public MiniRepository() {
        this(1024);
    }

    public MiniRepository(int initialCapacity) {
        this(initialCapacity, true);
    }
    
    public MiniRepository(int initialCapacity, boolean inverseIndex) {
        subjects = new HashMap<ID, PredicateCache>(initialCapacity);
        if (inverseIndex) {
            objects = new HashMap<ID, PredicateCache>(initialCapacity);
        }else{
            objects = null;
        }
    }
    
    public MiniRepository(STMT... stmts) {
        this(stmts.length);
        add(stmts);
    }
    
    public void add(STMT... stmts) {
        for (STMT stmt : stmts) {
            index(stmt.getSubject(), stmt, subjects);
            if (objects != null && stmt.getObject().isResource()) {
                index((ID) stmt.getObject(), stmt, objects);
            }
        }
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
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public void close() {
    }
    
    @Override
    public void export(Format format, OutputStream out) {        
        try {                        
            RDFWriter writer = format.createWriter(out);
            CloseableIterator<STMT> iterator = findStatements(null, null, null, null, false);
            try{
                writer.start();
                while (iterator.hasNext()){
                    writer.handle(iterator.next());
                }
                writer.end();
            }finally{
                iterator.close();                
                writer.close();
            }

        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }   
    }
    
    public CloseableIterator<STMT> findStatements(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, @Nullable UID context, boolean includeInferred) {
        Iterator<STMT> iterator = null;
        if (subject != null) {
            iterator = getIndexed(subject, predicate, subjects);
        } else if (objects != null && object != null && object.isResource()) {
            iterator = getIndexed(object.asResource(), predicate, objects);
        } else {
            IteratorChain<STMT> iterChain = new IteratorChain<STMT>();
            for (PredicateCache stmtCache : subjects.values()) {
                iterChain.addIterator(stmtCache.iterator(predicate));
            }
            iterator = iterChain;
        }
        return new ResultIterator(iterator, subject, predicate, object, context, includeInferred);
    }
    
    public MiniDialect getDialect() {
        return dialect;
    }

    private Iterator<STMT> getIndexed(ID key, UID predicate, Map<ID, PredicateCache> index) {
        PredicateCache stmtCache = index.get(key);
        if (stmtCache != null) {
            return stmtCache.iterator(predicate);
        } else {
            return Collections.<STMT>emptyList().iterator();
        }
    }
    
    public synchronized long getNextLocalId() {
        return ++localId;
    }

    public void index(ID key, STMT stmt, Map<ID, PredicateCache> index) {
        PredicateCache stmtCache = index.get(key);
        if (stmtCache == null) {
            stmtCache = new PredicateCache();
            index.put(key, stmtCache);
        } 
        stmtCache.add(stmt);
    }
    
    @Override
    public void initialize() {
    }
    
    public MiniConnection openConnection() {
        return new MiniConnection(this);
    }

    private boolean removeIndexed(ID key, STMT stmt, Map<ID, PredicateCache> index) {
        PredicateCache stmtMap = index.get(key);
        if (stmtMap != null) {
            return stmtMap.remove(stmt);
        } else {
            return false;
        }
    }

    public void removeStatement(STMT... stmts) {
        for (STMT stmt : stmts) {
            if (removeIndexed(stmt.getSubject(), stmt, subjects)) {
                if (objects != null && stmt.getObject().isResource()) {
                    removeIndexed((ID) stmt.getObject(), stmt, objects);
                }
            }
        }
    }

    @Override
    public void execute(Operation operation) {
        RDFConnection connection = openConnection();
        try{
            try{
                operation.execute(connection);
            }finally{
                connection.close();
            }    
        }catch(IOException io){
            throw new RepositoryException(io);
        }
    }
    
}
