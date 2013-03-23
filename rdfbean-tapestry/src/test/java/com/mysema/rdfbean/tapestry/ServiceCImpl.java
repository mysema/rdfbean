package com.mysema.rdfbean.tapestry;

import static org.junit.Assert.assertTrue;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.springframework.transaction.annotation.NotTransactional;

import com.mysema.rdfbean.object.SessionFactory;

public class ServiceCImpl implements ServiceC {

    @Inject
    private SessionFactory sessionFactory;

    public void txMethod() {
        assertTrue(sessionFactory.getCurrentSession() != null);
        assertTrue(sessionFactory.getCurrentSession().getTransaction().isActive());
    }

    @NotTransactional
    public void nonTxMethod() {
        assertTrue(sessionFactory.getCurrentSession() == null);
    }

}
