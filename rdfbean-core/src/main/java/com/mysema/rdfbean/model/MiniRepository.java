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

    public List<STMT> findStatements(ID subject, UID predicate, NODE object) {
        List<STMT> stmts = new ArrayList<STMT>();
        for (STMT stmt : statements){
            if (subject != null && !stmt.getSubject().equals(subject)){
                continue;
            }else if (predicate != null && !stmt.getPredicate().equals(predicate)){
                continue;
            }else if (object != null && !stmt.getObject().equals(object)){
                continue;
            }else{
                stmts.add(stmt);
            }
        }
        return stmts;
    }
    
    public MiniDialect getDialect() {
        return dialect;
    }

    public void removeStatement(STMT statement) {
        statements.remove(statement);
    }

}
