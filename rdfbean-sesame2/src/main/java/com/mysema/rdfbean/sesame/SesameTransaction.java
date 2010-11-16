/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * SesameTransaction provides an RDFBeanTransaction implementation for SesameConnection
 *
 * @author tiwe
 * @version $Id$
 */
public class SesameTransaction implements RDFBeanTransaction{
    
    private static final Logger logger = LoggerFactory.getLogger(SesameTransaction.class);
    
    private boolean active = false;
    
    private final SesameConnection connection;
    
    private boolean rollbackOnly;
    
    public SesameTransaction(SesameConnection connection, int isolationLevel) {
        this.connection = Assert.notNull(connection,"connection");
    }

    public void begin() {
        try {
            connection.getConnection().setAutoCommit(false);
        } catch (org.openrdf.repository.RepositoryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        active = true;
    }

    @Override
    public void commit() {
        if (rollbackOnly){
            throw new RepositoryException("Transaction is rollBackOnly");
        }        
        try {
            connection.getConnection().commit();
        } catch (org.openrdf.repository.RepositoryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }finally{
            connection.cleanUpAfterCommit();
        }
        
    }

    public boolean isActive(){
        return active;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    @Override
    public void prepare() {
        // TODO        
    }
    
    @Override
    public void rollback() {
       try {
           connection.getConnection().rollback();           
       } catch (org.openrdf.repository.RepositoryException e) {
           String error = "Caught " + e.getClass().getName();
           logger.error(error, e);
           throw new RepositoryException(error, e);
       }finally{
           connection.cleanUpAfterRollback();
       }        
    }

    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
        
    }

}
