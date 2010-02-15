/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * RepositoryIdentityService uses the Repository to store the local ids
 *
 * @author tiwe
 * @version $Id$
 */
public class RepositoryIdentityService implements IdentityService{

    private final RDFConnection connection;
    
    public RepositoryIdentityService(RDFConnection connection){
        this.connection = connection;
    }
    
    @Override
    public ID getID(LID lid) {
        STMT stmt = find(null, CORE.localId, new LIT(lid.getId()));
        if (stmt != null){
            return stmt.getSubject();
        }else{
            throw new IllegalArgumentException("No ID for " + lid);
        }
    }
    
    private LID getLID(ID id){
        STMT stmt = find(id, CORE.localId, null);
        if (stmt != null){
            return new LID(stmt.getObject().getValue());
        }else{
            LIT lit = new LIT(UUID.randomUUID().toString()); // TODO : short numeric value
            add(id, CORE.localId, lit);
            return new LID(lit.getValue());
        }
    }

    @Override
    public LID getLID(UID id) {
        return getLID((ID)id);
    }

    @Override
    public LID getLID(@Nullable /* always null */ ID model, BID id) {
        return getLID(id);
    }

    
    private void add(ID subject, UID predicate, NODE object){
        connection.update(
                Collections.<STMT>emptySet(), 
                Collections.singleton(new STMT(subject, predicate, object)));
    }

    private @Nullable STMT find(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object){
        CloseableIterator<STMT> stmts = connection
            .findStatements(subject, predicate, object, null, false);
        try{
            if (stmts.hasNext()){
                return stmts.next();
            }else{
                return null;
            }
        }finally{
            try {
                stmts.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
