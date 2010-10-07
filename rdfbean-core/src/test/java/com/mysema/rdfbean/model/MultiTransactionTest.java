/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.object.Session;

/**
 * MultiTransactionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MultiTransactionTest {
    
    private MultiTransaction tx;
    
    @Before
    public void setUp(){
	MiniRepository repository = new MiniRepository();
        MultiConnection connection = new MultiConnection(
                repository.openConnection(),
                repository.openConnection()
            ){
                @Override
                public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
                    throw new UnsupportedOperationException();
                }
                
                @Override
                public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
                    throw new UnsupportedOperationException();
                }
        };
        
        RDFBeanTransaction innerTx = EasyMock.createNiceMock(RDFBeanTransaction.class);        
        tx = new MultiTransaction(connection, new RDFBeanTransaction[]{innerTx});
    }
    
    @Test
    public void testCommit() {
        tx.commit();
    }

    @Test
    public void testIsActive() {
        tx.isActive();
    }

    @Test
    public void testIsRollbackOnly() {
        tx.isRollbackOnly();
    }

    @Test
    public void testRollback() {
        tx.rollback();
    }

    @Test
    public void testSetRollbackOnly() {
        tx.setRollbackOnly();
    }

}
