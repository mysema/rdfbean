/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;


/**
 * RDFBeanTransaction provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface RDFBeanTransaction {

    /**
     * Flush the session, commit and close the connection.
     *
     * @throws OtmException on an error in commit
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
     * @throws OtmException on an error in roll-back
     */
    public void rollback();

    /**
     * Mark the transaction for rollback.
     */
    public void setRollbackOnly();
}
