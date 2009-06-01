/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorWrapper;

/**
 * @author sasa
 *
 */
public final class MiniRepository implements Repository<MiniDialect> {
    
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
        statements.addAll(Arrays.asList(stmts));
    }

    public CloseableIterator<STMT> findStatements(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        List<STMT> stmts = new ArrayList<STMT>();
        for (STMT stmt : statements){
            if (
                    // Subject match
                    (subject == null || stmt.getSubject().equals(subject)) &&

                    // Predicate match
                    (predicate == null || predicate.equals(stmt.getPredicate())) &&
                    
                    // Object match
                    (object == null || object.equals(stmt.getObject())) &&
                    
                    // Context match
                    (context == null || context.equals(stmt.getContext())) &&
                    
                    // Asserted or includeInferred statement
                    (includeInferred || stmt.isAsserted())
            ) {
                stmts.add(stmt);
            }
        }
        return new IteratorWrapper<STMT>(stmts.iterator());
    }
    
    public MiniDialect getDialect() {
        return dialect;
    }

    public void removeStatement(STMT... stmts) {
        statements.removeAll(Arrays.asList(stmts));
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
