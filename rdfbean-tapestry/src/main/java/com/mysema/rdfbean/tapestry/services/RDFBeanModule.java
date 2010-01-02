/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry.services;

import java.io.IOException;
import java.util.Map;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ServiceBinder;

import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.object.identity.DerbyIdentityService;
import com.mysema.rdfbean.object.identity.IdentityService;
import com.mysema.rdfbean.tapestry.TransactionalAdvisor;
import com.mysema.rdfbean.tapestry.TransactionalAdvisorImpl;

/**
 * RDFBeanModule defines an abstract module for Tapestry IoC with a basic RDFBean configuration
 *
 * @author tiwe
 * @version $Id$
 */
public class RDFBeanModule {

    public static final String DERBY_URL = "identityService.derbyUrl";
    
    public static void bind(ServiceBinder binder){
        binder.bind(TransactionalAdvisor.class, TransactionalAdvisorImpl.class);
        binder.bind(SeedEntity.class, SeedEntityImpl.class);
    }

    public static void adviseTransactions(TransactionalAdvisor advisor, MethodAdviceReceiver receiver){
        advisor.addTransactionCommitAdvice(receiver);
    }
    
    public static IdentityService buildIdentityService(Map<String,String> configuration) throws IOException{
        if (!configuration.containsKey(DERBY_URL)){
            throw new IllegalArgumentException(DERBY_URL + " parameter is missing");
        }
        return new DerbyIdentityService(configuration.get(DERBY_URL));
    }
    
    public static void contributeIdentityService(final MappedConfiguration<String, String> configuration) {
        configuration.add(DERBY_URL, "jdbc:derby:target/test/testids;create=true") ;
    }

    public static SessionFactory buildSessionFactory(Configuration configuration, Repository repository){        
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory;
    }

}
