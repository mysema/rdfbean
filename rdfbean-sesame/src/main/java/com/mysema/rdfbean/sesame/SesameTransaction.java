/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.object.RDFBeanTransaction;

/**
 * SesameTransaction provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SesameTransaction implements RDFBeanTransaction{

    private static final Logger logger = LoggerFactory.getLogger(SesameTransaction.class);
    
    private SesameSession session;
    
    private boolean rollBackOnly;
    
    private boolean active = false;
    
    public SesameTransaction(SesameSession session, int isolationLevel) {
        this.session = Assert.notNull(session);
    }

    @Override
    public void commit() {
        if (rollBackOnly){
            throw new RuntimeException("Transaction is rollBackOnly");
        }        
        try {
            session.getConnection().commit();
        } catch (StoreException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }finally{
            session.cleanUpAfterCommit();
        }
        
    }

    @Override
    public boolean isRollbackOnly() {
        return rollBackOnly;
    }

    @Override
    public void rollback() {
       try {
           session.getConnection().rollback();           
       } catch (StoreException e) {
           String error = "Caught " + e.getClass().getName();
           logger.error(error, e);
           throw new RuntimeException(error, e);
       }finally{
           session.cleanUpAfterRollback();
       }        
    }

    @Override
    public void setRollbackOnly() {
        this.rollBackOnly = true;
        
    }

    public void begin() {
        try {
//            session.getConnection().setAutoCommit(false);
//            session.getConnection().setTransactionIsolation(isolation);
            session.getConnection().begin();            
        } catch (StoreException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        active = true;
    }
    
    public boolean isActive(){
        return active;
    }

}
