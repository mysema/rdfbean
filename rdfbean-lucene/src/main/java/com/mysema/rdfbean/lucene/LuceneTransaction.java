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
public class LuceneTransaction implements RDFBeanTransaction{
    
    private final CompassTransaction tx;
    
    private boolean rollBackOnly;
    
    private boolean active = true;
    
    public LuceneTransaction(CompassTransaction tx) {
        this.tx = Assert.notNull(tx);
    }

    @Override
    public void commit() {
        try {
            tx.commit();
        }finally{
            active = false;
        }    
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollBackOnly;
    }

    @Override
    public void rollback() {
        try {
            tx.rollback();
        }finally{
            active = false;
        }
    }

    @Override
    public void setRollbackOnly() {
        this.rollBackOnly = true;
        
    }

}
