/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mysema.rdfbean.model.Ontology;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.DefaultOntology;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.object.identity.DerbyIdentityService;
import com.mysema.rdfbean.object.identity.IdentityService;

/**
 * RDFBeanModule provides an abstract base class for RDFBean based Guice modules
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class RDFBeanModule extends AbstractModule{

    private static final Logger logger = LoggerFactory.getLogger(RDFBeanModule.class);
    
    public List<String> getConfiguration(){
        return Collections.singletonList("/persistence.properties");
    }
    
    @Override
    protected void configure() {               
        // inject properties
        try {
            Properties properties = new Properties();
            for (String res : getConfiguration()){
                properties.load(RDFBeanModule.class.getResourceAsStream(res));
            }            
            for (Object key : properties.keySet()){
                bind(String.class)
                    .annotatedWith(Names.named(key.toString()))
                    .toInstance(properties.getProperty(key.toString()));
            }
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
       
        // AOP tx handling
        TransactionalMethodMatcher methodMatcher = new TransactionalMethodMatcher();
        TransactionalInterceptor interceptor = new TransactionalInterceptor(methodMatcher);
        requestInjection(interceptor);
        bindInterceptor(Matchers.any(), methodMatcher, interceptor);
    }
    
    @Provides
    @Singleton
    public IdentityService createIdentityService(@Named("identityService.derby.url") String url) 
        throws IOException{
        return new DerbyIdentityService(url);
    }
    
    @Provides 
    @Singleton
    public abstract Repository createRepository(Ontology ontology);
    
    @Provides
    @Singleton
    public Ontology createOntology(Configuration configuration){
        return new DefaultOntology(configuration); 
    }
    
    @Provides
    @Singleton
    public Configuration createConfiguration(IdentityService identityService){
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.addClasses(getAnnotatedClasses());
        configuration.addPackages(getAnnotatedPackages());
        configuration.setIdentityService(identityService);
        return configuration;
    }
    
    protected Package[] getAnnotatedPackages() {
        return new Package[0];
    }

    protected Class<?>[] getAnnotatedClasses() {
        return new Class<?>[0];
    }

    @Provides
    @Singleton
    public SessionFactory createSessionFactory(Configuration configuration, Repository repository){        
        // TODO : locale handling
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory;
    }

}
