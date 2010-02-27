/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.mulgara;

import org.mulgara.connection.Connection;
import org.mulgara.query.QueryException;
import org.mulgara.query.operation.Commit;
import org.mulgara.query.operation.Rollback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.RDFBeanTransaction;

/**
 * MulgaraTransaction provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MulgaraTransaction implements RDFBeanTransaction{

    private static final Logger logger = LoggerFactory.getLogger(MulgaraTransaction.class);
    
    private final MulgaraConnection mulgaraConnection;
    
    private final Connection connection;
    
    private boolean active = true;
    
    private boolean rollbackOnly;
    
    public MulgaraTransaction(MulgaraConnection mulgaraConnection, Connection connection, boolean readOnly,
            int txTimeout, int isolationLevel) {        
        try {
            this.mulgaraConnection = Assert.notNull(mulgaraConnection);
            this.connection = Assert.notNull(connection);
            connection.setAutoCommit(false);
            connection.getSession().setTransactionTimeout(txTimeout);
        } catch (QueryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }        
    }

    @Override
    public void commit() {
        if (rollbackOnly){
            throw new RuntimeException("Transaction is rollBackOnly");
        }    
        try {
            connection.execute(new Commit());
        } catch (Exception e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }finally{
            mulgaraConnection.cleanUpAfterCommit();
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
        // TODO
        
    }

    @Override
    public void rollback() {
        try {
            connection.execute(new Rollback());
        } catch (Exception e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }finally{
            mulgaraConnection.cleanUpAfterRollback();
        }
        
    }

    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;
        
    }

}
