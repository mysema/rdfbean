/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry.services;

import java.io.IOException;
import java.util.Map;

import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.mysema.rdfbean.model.Ontology;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultOntology;
import com.mysema.rdfbean.object.ObjectRepository;
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

    public static IdentityService buildIdentityService(@Inject @Symbol(DERBY_URL) String derbyURL) throws IOException{
        return new DerbyIdentityService(derbyURL);
    }
    
    public static Ontology buildOntology(Configuration configuration){
        return new DefaultOntology(configuration);
    }
    
    public static SessionFactory buildSessionFactory(Configuration configuration, Repository repository, 
            Map<String,ObjectRepository> objectRepositories){        
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();        
        sessionFactory.setObjectRepositories(objectRepositories);
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory;
    }

}
