/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import org.compass.core.CompassTransaction;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.object.RDFBeanTransaction;

/**
 * LuceneTransaction provides
 *
 * @author tiwe
 * @version $Id$
 */
class LuceneTransaction implements RDFBeanTransaction{
    
    private boolean active = true;
    
    private final LuceneConnection conn;
    
    private boolean rollbackOnly;
    
    private final CompassTransaction tx;
    
    public LuceneTransaction(LuceneConnection conn, CompassTransaction tx) {
        this.conn = Assert.notNull(conn);
        this.tx = Assert.notNull(tx);
    }

    @Override
    public void commit() {
        if (rollbackOnly){
            throw new RuntimeException("Transaction is rollBackOnly");
        }   
        try {
            tx.commit();
        }finally{
            conn.cleanUpAfterCommit();
            active = false;
        }    
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    @Override
    public void prepare() {
        tx.getSession().flush();        
    }

    @Override
    public void rollback() {
        try {
            tx.rollback();
        }finally{
            conn.cleanUpAfterRollback();
            active = false;
        }
    }

    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
        
    }

}
