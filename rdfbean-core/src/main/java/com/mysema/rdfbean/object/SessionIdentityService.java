/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Collections;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * SessionIdentityService uses the Repository to store the local ids
 *
 * @author tiwe
 * @version $Id$
 */
public class SessionIdentityService implements IdentityService{

    private final RDFConnection connection;
    
    public SessionIdentityService(RDFConnection connection){
        this.connection = connection;
    }
    
    @Override
    public ID getID(LID lid) {
        STMT stmt = find(null, CORE.localId, new LIT(lid.getId()));
        return stmt != null ? stmt.getSubject() : null;
    }
    
    @Override
    public LID getLID(ID id){
        STMT stmt = find(id, CORE.localId, null);
        String lid;
        if (stmt != null){
            lid = stmt.getObject().getValue();
        }else{
            lid = String.valueOf(connection.getNextLocalId());
            add(id, CORE.localId, new LIT(lid));
        }
        return new LID(lid);
    }
    
    private void add(ID subject, UID predicate, NODE object){
        connection.update(
                null, 
                Collections.singleton(new STMT(subject, predicate, object)));
    }

    @Nullable
    private STMT find(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object){
        CloseableIterator<STMT> stmts = connection.findStatements(subject, predicate, object, null, false);
        try{
            return stmts.hasNext() ? stmts.next() : null;
        }finally{
            stmts.close();
        }
    }
}
