/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections15.iterators.IteratorChain;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFWriter;
import com.mysema.rdfbean.model.io.WriterUtils;

/**
 * MiniRepository is a lightweight implementation of the Repository interface
 * for use in local cacheing of statements and tests
 * 
 * @author sasa
 *
 */
public final class MiniRepository implements Repository{
    
    private final MiniDialect dialect = new MiniDialect();
    
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

    @Override
    public void close() {

    }
    
    @Override
    public void load(Format format, InputStream is, @Nullable UID context, boolean replace) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void export(Format format, Map<String, String> ns2prefix, UID context, OutputStream out) {
        RDFWriter writer = WriterUtils.createWriter(format, out, ns2prefix);
        RDFConnection conn = openConnection();
        try{                
            CloseableIterator<STMT> stmts = conn.findStatements(null, null, null, context, false);
            try{
                writer.begin();
                while (stmts.hasNext()){
                    writer.handle(stmts.next());
                }
                writer.end();
            }finally{
                stmts.close();
            }
            
        }finally{
            conn.close();
        }            
    }
    
    @Override
    public void export(Format format, UID context, OutputStream out) {        
        export(format, Namespaces.DEFAULT, context, out);
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
    
    public boolean exists(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, @Nullable UID context) {
        if (subject != null){
            return subjects.containsKey(subject);
        }else if (objects != null && object != null && object.isResource()){
            return objects.containsKey(object.asResource());
        }else{
            for (PredicateCache stmtCache : subjects.values()) {
                if (stmtCache.iterator(predicate).hasNext()){
                    return true;
                }
            }
            return false;
        }
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


    public void remove(@Nullable ID subject, @Nullable UID predicate, @Nullable  NODE object, @Nullable UID context) {
        // remove all
        if (subject == null && predicate == null && object == null && context == null){
            subjects.clear();
            if (objects != null){
                objects.clear();
            }
            
        // subject given    
        }else if (subject != null){
            PredicateCache cache = subjects.get(subject);
            if (cache != null){
                Iterator<STMT> stmts = cache.iterator(predicate);                
                removeStatements(IteratorAdapter.asList(stmts));
                    
            }            
            
        // object given   
        }else if (object != null && object.isResource() && objects != null){
            PredicateCache cache = objects.get(object.asResource());
            if (cache != null){
                Iterator<STMT> stmts = cache.iterator(predicate);                
                removeStatements(IteratorAdapter.asList(stmts));    
            }
            
        // predicate or context given    
        }else{
            for (Map.Entry<ID, PredicateCache> entry : subjects.entrySet()){
                PredicateCache cache = entry.getValue();
                Iterator<STMT> stmts = cache.iterator(predicate);                
                removeStatements(IteratorAdapter.asList(stmts));
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

    public void removeStatements(Collection<STMT> stmts) {
        for (STMT stmt : stmts) {
            if (removeIndexed(stmt.getSubject(), stmt, subjects)) {
                if (objects != null && stmt.getObject().isResource()) {
                    removeIndexed((ID) stmt.getObject(), stmt, objects);
                }
            }
        }
    }
    
    @Override
    public <RT> RT execute(Operation<RT> operation) {
        RDFConnection connection = openConnection();
        try{
            try{
                return operation.execute(connection);
            }finally{
                connection.close();
            }    
        }catch(IOException io){
            throw new RepositoryException(io);
        }
    }
    
}
