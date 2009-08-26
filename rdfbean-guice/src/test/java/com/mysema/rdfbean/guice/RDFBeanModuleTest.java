package com.mysema.rdfbean.guice;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.sesame.MemoryRepository;


/**
 * RDFBeanModuleTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDFBeanModuleTest {

    private static Injector injector;
    
    @BeforeClass
    public static void setUp(){
        injector = Guice.createInjector(new RDFBeanModule(){
            @Override
            protected void configure() {
                super.configure();
                bind(ServiceA.class).in(Scopes.SINGLETON);
                bind(ServiceB.class).to(ServiceBImpl.class).in(Scopes.SINGLETON);
            }
            @Override
            public Repository repository() {
                return new MemoryRepository();
            }            
        });
    }
    
    @Test
    public void tx1(){
        ServiceA serviceA = injector.getInstance(ServiceA.class);
        serviceA.txMethod();
        serviceA.txReadonly();
        serviceA.nonTxMethod();
    }
    

    @Test
    @Ignore
    public void tx2(){
        // FIXME
        ServiceB serviceB = injector.getInstance(ServiceB.class); 
        serviceB.txMethod();
        serviceB.txReadonly();
        serviceB.nonTxMethod();
    }
    
    
}
