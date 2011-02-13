package com.mysema.rdfbean.guice;

import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;
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
