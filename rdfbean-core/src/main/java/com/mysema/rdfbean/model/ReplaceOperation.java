/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 * 
 */
public class ReplaceOperation implements Operation<Void> {

    private final Map<UID, UID> replacements;

    public ReplaceOperation(Map<UID, UID> replacements) {
        this.replacements = replacements;
    }

    @Override
    public Void execute(RDFConnection connection) throws IOException {
        for (Map.Entry<UID, UID> entry : replacements.entrySet()) {
            Set<STMT> added = new HashSet<STMT>();
            Set<STMT> removed = new HashSet<STMT>();
            
            // subjects
            CloseableIterator<STMT> stmts = connection.findStatements(entry.getKey(), null, null, null, false);
            try {
                while (stmts.hasNext()) {
                    STMT stmt = stmts.next();
                    removed.add(stmt);
                    added.add(new STMT(entry.getValue(), stmt.getPredicate(),stmt.getObject(), stmt.getContext()));
                }
            } finally {
                stmts.close();
            }
            
            // objects
            stmts = connection.findStatements(null, null, entry.getKey(), null, false);
            try {
                while (stmts.hasNext()) {
                    STMT stmt = stmts.next();
                    removed.add(stmt);
                    added.add(new STMT(stmt.getSubject(), stmt.getPredicate(),entry.getValue(), stmt.getContext()));
                }
            } finally {
                stmts.close();
            }
            
            // update
            connection.update(removed, added);
        }
        return null;
    }
    
}
