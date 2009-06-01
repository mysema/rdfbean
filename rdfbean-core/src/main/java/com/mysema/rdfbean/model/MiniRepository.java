/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

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

    public CloseableIterator<STMT> findStatements(ID subject, UID predicate, NODE object, UID context) {
        List<STMT> stmts = new ArrayList<STMT>();
        for (STMT stmt : statements){
            if (subject != null && !stmt.getSubject().equals(subject)) {
                continue;
            } else if (predicate != null && !predicate.equals(stmt.getPredicate())) {
                continue;
            } else if (object != null && !object.equals(stmt.getObject())) {
                continue;
            } else if (context != null && !context.equals(stmt.getContext())) {
                continue;
            } else {
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
    
}
