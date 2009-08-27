/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
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
                bind(ServiceA.class);
                bind(ServiceB.class).to(ServiceBImpl.class);
                bind(ServiceC.class);
                bind(ServiceD.class).to(ServiceDImpl.class);
            }
            @Override
            public Repository repository() {
                return new MemoryRepository();
            }            
        });
    }
    
    @Test
    public void tx1(){
        ServiceA service = injector.getInstance(ServiceA.class);
        service.nonTxMethod();
        service.nonTxMethod2();
        service.txMethod();
        service.txMethod2();
        service.txMethod3();        
        service.txReadonly();        
    }
    
    @Test
    public void tx2(){
        ServiceB service = injector.getInstance(ServiceB.class); 
        service.txMethod();
        service.txReadonly();
        service.nonTxMethod();
    }
    
    @Test
    public void tx3(){
        ServiceC service = injector.getInstance(ServiceC.class); 
        service.txMethod();
        service.nonTxMethod();
    }
    
    @Test
    public void tx4(){
        ServiceD service = injector.getInstance(ServiceD.class); 
        service.txMethod();
    }
}
