/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * ServiceA provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ServiceA{
    
    @Inject 
    private SessionFactory sessionFactory;
    
    @Transactional
    public void txMethod(){
        assertTrue(sessionFactory.getCurrentSession() != null);
    }
    
    @Transactional(type=TransactionType.READ_ONLY)
    public void txReadonly(){
        assertTrue(sessionFactory.getCurrentSession() != null);
    }
    
    public void nonTxMethod(){
        assertTrue(sessionFactory.getCurrentSession() == null);
    }
}