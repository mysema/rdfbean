/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openrdf.store.StoreException;

import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.sesame.query.QSimpleType;
import com.mysema.rdfbean.sesame.query.QSimpleType2;

/**
 * @author sasa
 *
 */
public class SessionTestBase {
    
    protected static final QSimpleType var = new QSimpleType("var");
    
    protected static final QSimpleType2 var2 = new QSimpleType2("var2");
    
    protected static final Locale FI = new Locale("fi");

    protected static final Locale EN = new Locale("en");

    protected static MemoryRepository repository;
    
    private static SessionFactory sessionFactory;
    
    protected Session session;
    
    private List<Session> openSessions = new ArrayList<Session>();
    
    @BeforeClass
    public static void before(){
        repository = new MemoryRepository();
        repository.setSources(
                new RDFSource("classpath:/test.ttl", Format.TURTLE, TEST.NS),
                new RDFSource("classpath:/foaf.rdf", Format.RDFXML, FOAF.NS)
        );
        repository.initialize();
    }
    
    @AfterClass
    public static void after(){
        try{
            if (sessionFactory != null) sessionFactory.close();
            repository.close();    
        }finally{
            sessionFactory = null;
            repository = null;
        }        
    }
    
    @After
    public void tearDown() throws IOException{
        for (Session s : openSessions){
            s.close();
        }
        session = null;
        System.out.println();
    }
    
    protected Session createSession(Package... packages) throws StoreException, ClassNotFoundException {
        return createSession(EN, packages);
    }

    protected Session createSession(Locale locale, Package... packages) throws StoreException, ClassNotFoundException {
        if (sessionFactory == null){
            sessionFactory = createSessionFactory(locale, new DefaultConfiguration(packages));
        }
        Session rv = sessionFactory.openSession();
        openSessions.add(rv);
        return rv;
    }

    protected Session createSession(Class<?>... classes) throws StoreException {
        return createSession(EN, classes);
    }

    protected Session createSession(Locale locale, Class<?>... classes) throws StoreException {
        if (sessionFactory == null){
            sessionFactory = createSessionFactory(locale, new DefaultConfiguration(classes));
        }
        Session rv = sessionFactory.openSession();
        openSessions.add(rv);
        return rv;
    }
    
    private SessionFactory createSessionFactory(Locale locale, Configuration configuration){
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl(Collections.singletonList(locale));
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory;
    }
    
    protected BeanQuery from(PEntity<?>... entities){
        return session.from(entities);
    }
}
