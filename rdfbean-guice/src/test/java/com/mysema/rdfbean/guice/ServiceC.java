package com.mysema.rdfbean.guice;

import static org.junit.Assert.assertTrue;

import org.springframework.transaction.annotation.NotTransactional;
import org.springframework.transaction.annotation.Transactional;

import com.google.inject.Inject;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * ServiceC provides
 *
 * @author tiwe
 * @version $Id$
 */
@Transactional
public class ServiceC {

    @Inject 
    private SessionFactory sessionFactory;
    
    public void txMethod(){
        assertTrue(sessionFactory.getCurrentSession() != null);
        assertTrue(sessionFactory.getCurrentSession().getTransaction().isActive());
    }
    
    @NotTransactional
    public void nonTxMethod(){
        assertTrue(sessionFactory.getCurrentSession() == null);
    }

}
