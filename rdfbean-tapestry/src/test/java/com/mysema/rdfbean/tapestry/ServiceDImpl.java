package com.mysema.rdfbean.tapestry;

import static org.junit.Assert.assertTrue;

import org.apache.tapestry5.ioc.annotations.Inject;

import com.mysema.rdfbean.object.SessionFactory;

public class ServiceDImpl implements ServiceD{

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public void txMethod(){
        assertTrue(sessionFactory.getCurrentSession() != null);
        assertTrue(sessionFactory.getCurrentSession().getTransaction().isActive());
    }

}
