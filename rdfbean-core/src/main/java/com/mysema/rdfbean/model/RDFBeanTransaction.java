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
    void prepare();
    
    /**
     * Commit and close the connection.
     *
     * @throws RDFBeanException on an error in commit
     */
    void commit();
    
    /**
     * 
     * @return
     */
    boolean isActive();

    /**
     * Test whether this transaction has been marked for rollback only.
     *
     * @return true if this transaction will be rolled back
     */
    boolean isRollbackOnly();

    /**
     * Rollback the transaction and close the connection. Session data is left alone.
     *
     * @throws RDFBeanException on an error in roll-back
     */
    void rollback();

    /**
     * Mark the transaction for rollback.
     */
    void setRollbackOnly();
}
