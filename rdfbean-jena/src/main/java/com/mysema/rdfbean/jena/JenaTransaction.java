package com.mysema.rdfbean.jena;

import com.hp.hpl.jena.graph.TransactionHandler;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * @author tiwe
 * 
 */
public class JenaTransaction implements RDFBeanTransaction {

    private final TransactionHandler transactionHandler;

    private boolean rollbackOnly;

    private boolean active = true;

    public JenaTransaction(TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
        transactionHandler.begin();
    }

    @Override
    public void commit() {
        if (rollbackOnly) {
            throw new RepositoryException("Transaction is rollBackOnly");
        }
        transactionHandler.commit();
        active = false;
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

    }

    @Override
    public void rollback() {
        transactionHandler.abort();
        active = false;
    }

    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

}
