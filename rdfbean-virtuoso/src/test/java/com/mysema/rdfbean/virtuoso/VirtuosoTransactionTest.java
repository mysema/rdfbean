package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;

public class VirtuosoTransactionTest extends AbstractConnectionTest{
    
    private RDFBeanTransaction tx;
    
    @Override
    @Before
    public void setUp(){
        super.setUp();
        tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
    }
    
    @Test
    public void Commit() {
        tx.commit();
    }

    @Test
    public void IsActive() {
        assertTrue(tx.isActive());
    }

    @Test
    public void IsRollbackOnly() {
        assertFalse(tx.isRollbackOnly());
        tx.setRollbackOnly();
        assertTrue(tx.isRollbackOnly());
    }

    @Test
    public void Prepare() {
        tx.prepare();
    }

    @Test
    public void Rollback() {
        tx.rollback();
    }

    @Test(expected=RepositoryException.class)
    public void SetRollbackOnly() {
        tx.setRollbackOnly();
        tx.commit();
    }

}
