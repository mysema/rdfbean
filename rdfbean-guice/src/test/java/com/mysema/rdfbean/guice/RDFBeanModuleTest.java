package com.mysema.rdfbean.guice;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.sesame.MemoryRepository;


/**
 * RDFBeanModuleTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDFBeanModuleTest {

    @Test
    public void test(){
        Injector injector = Guice.createInjector(new RDFBeanModule(){
            @Override
            protected void configure() {
                super.configure();
                bind(Service.class).in(Scopes.SINGLETON);
            }
            @Override
            public Repository repository() {
                return new MemoryRepository();
            }            
        });
        SessionFactory factory = injector.getInstance(SessionFactory.class);
        assertNotNull(factory);
        Service service = injector.getInstance(Service.class);
        service.txMethod();
        service.txReadonly();
        service.nonTxMethod();
        
    }
    
    static class Service{
        
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
    
}
