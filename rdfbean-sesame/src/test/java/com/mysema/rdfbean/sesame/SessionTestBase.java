/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.SimpleDomain;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.testutil.SessionRule;

/**
 * @author sasa
 *
 */
public class SessionTestBase implements SimpleDomain{
    
    protected static final QSimpleType var = new QSimpleType("var");
    
    protected static final QSimpleType2 var2 = new QSimpleType2("var2");
    
    protected static MemoryRepository repository;
    
    private static SessionFactory sessionFactory;
    
    @Rule
    public SessionRule sessionRule = new SessionRule(repository);
    
    public Session session;
    
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
            if (repository != null) repository.close();    
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
        System.out.println();
    }
    
}
