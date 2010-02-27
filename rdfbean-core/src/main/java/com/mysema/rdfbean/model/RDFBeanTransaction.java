/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;


/**
 * RDFBeanTransaction defines a common interface for RDFBean transaction implementations
 *
 * @author tiwe
 * @version $Id$
 */
public interface RDFBeanTransaction {
    
    /**
     * Prepare the transaction for commit
     */
    public void prepare();
    
    /**
     * Commit and close the connection.
     *
     * @throws RDFBeanException on an error in commit
     */
    public void commit();
    
    /**
     * 
     * @return
     */
    public boolean isActive();

    /**
     * Test whether this transaction has been marked for rollback only.
     *
     * @return true if this transaction will be rolled back
     */
    public boolean isRollbackOnly();

    /**
     * Rollback the transaction and close the connection. Session data is left alone.
     *
     * @throws RDFBeanException on an error in roll-back
     */
    public void rollback();

    /**
     * Mark the transaction for rollback.
     */
    public void setRollbackOnly();
}
