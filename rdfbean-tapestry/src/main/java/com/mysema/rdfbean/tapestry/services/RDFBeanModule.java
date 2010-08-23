/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.tapestry.services;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections15.BeanMap;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.services.ValueEncoderFactory;

import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.ObjectRepository;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.tapestry.TransactionalAdvisor;
import com.mysema.rdfbean.tapestry.TransactionalAdvisorImpl;

/**
 * RDFBeanModule defines an abstract module for Tapestry IoC with a basic RDFBean configuration
 *
 * @author tiwe
 * @version $Id$
 */
public final class RDFBeanModule {

    private RDFBeanModule(){}

    public static void bind(ServiceBinder binder){
        binder.bind(TransactionalAdvisor.class, TransactionalAdvisorImpl.class);
        binder.bind(SeedEntity.class, SeedEntityImpl.class);
    }

    public static SessionFactory buildSessionFactory(
            Configuration configuration,
            Repository repository,
            Map<String,ObjectRepository> objectRepositories){
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setObjectRepositories(objectRepositories);
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory;
    }

    public static void contributeValueEncoderSource(
            MappedConfiguration<Class<?>, ValueEncoderFactory<?>> configuration,
            com.mysema.rdfbean.object.Configuration rdfBeanConfiguration,
            SessionFactory sessionFactory){

        for (MappedClass mappedClass : rdfBeanConfiguration.getMappedClasses()){
            if (mappedClass.getIdProperty() != null){
                Class<?> clazz = mappedClass.getJavaClass();
                configuration.add(clazz, createValueEncoder(mappedClass.getIdProperty(), sessionFactory, clazz));
            }
        }
    }

    private static <T> ValueEncoderFactory<T> createValueEncoder(
            final MappedProperty<?> idProperty,
            final SessionFactory sessionFactory,
            final Class<T> clazz) {

        return new ValueEncoderFactory<T>(){
            @Override
            public ValueEncoder<T> create(Class<T> type) {
                return new ValueEncoder<T>(){
                    @Override
                    public String toClient(T value) {
                        // TODO : handle other id types as well
                        return idProperty.getValue(new BeanMap(value)).toString();
                    }
                    @Override
                    public T toValue(String id) {
                        // thread bound session
                        if (sessionFactory.getCurrentSession() != null){
                            return sessionFactory.getCurrentSession().getById(id, clazz);
                        // new session
                        }else{
                            Session session = sessionFactory.openSession();
                            try{
                                return session.getById(id, clazz);
                            }finally{
                                close(session);
                            }
                        }
                    }
                };
            }
        };
    }

    private static void close(Closeable closeable){
        try {
            closeable.close();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

}
