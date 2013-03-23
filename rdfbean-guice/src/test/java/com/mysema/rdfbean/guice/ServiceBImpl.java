/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;
import com.mysema.rdfbean.object.SessionFactory;

public class ServiceBImpl implements ServiceB {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public void txMethod() {
        assertTrue(sessionFactory.getCurrentSession() != null);
        assertTrue(sessionFactory.getCurrentSession().getTransaction().isActive());
    }

    @Override
    public void nonTxMethod() {
        assertTrue(sessionFactory.getCurrentSession() == null);
    }

    @Override
    public void txReadonly() {
        assertTrue(sessionFactory.getCurrentSession() != null);
        assertTrue(sessionFactory.getCurrentSession().getTransaction().isActive());
    }

}
