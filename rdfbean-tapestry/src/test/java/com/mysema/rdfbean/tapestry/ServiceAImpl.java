/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.tapestry;

import static org.junit.Assert.assertTrue;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.rdfbean.object.SessionFactory;

public class ServiceAImpl implements ServiceA {

    @Inject
    private SessionFactory sessionFactory;

    public void nonTxMethod() {
        assertTrue(sessionFactory.getCurrentSession() == null);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void nonTxMethod2() {
        // not intercepted
        assertTrue(sessionFactory.getCurrentSession() == null);
    }

    @Transactional
    public void txMethod() {
        assertTrue(sessionFactory.getCurrentSession() != null);
        assertTrue(sessionFactory.getCurrentSession().getTransaction().isActive());
    }

    @Transactional(propagation = Propagation.NEVER)
    public void txMethod2() {
        assertTrue(sessionFactory.getCurrentSession() == null);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void txMethod3() {
        assertTrue(sessionFactory.getCurrentSession() == null);
    }

    @Transactional(readOnly = true)
    public void txReadonly() {
        assertTrue(sessionFactory.getCurrentSession() != null);
        assertTrue(sessionFactory.getCurrentSession().getTransaction().isActive());
    }

    @Transactional
    public void txMethodWithException_commit() throws Exception {
        throw new Exception();
    }

    @Transactional(rollbackFor = Exception.class)
    public void txMethodWithException_rollback() throws Exception {
        throw new Exception();
    }
}