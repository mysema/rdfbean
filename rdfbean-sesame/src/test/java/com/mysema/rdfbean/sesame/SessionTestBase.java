/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.SimpleDomain;
import com.mysema.rdfbean.domains.NoteTypeDomain.NoteType;
import com.mysema.rdfbean.model.FOAF;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;
import com.mysema.rdfbean.testutil.SessionRule;

/**
 * @author sasa
 * 
 */
public abstract class SessionTestBase implements SimpleDomain {

    protected static final QSimpleType var = new QSimpleType("var");

    protected static final QSimpleType2 var2 = new QSimpleType2("var2");

    protected static MemoryRepository repository;

    private static SessionFactory sessionFactory;

    @Rule
    public SessionRule sessionRule = new SessionRule(repository);

    public Session session;

    private List<Session> openSessions = new ArrayList<Session>();

    @BeforeClass
    public static void before() throws IOException {
        repository = new MemoryRepository();
        repository.setSources(
                new RDFSource("classpath:/test.ttl", Format.TURTLE, TEST.NS),
                new RDFSource("classpath:/foaf.rdf", Format.RDFXML, FOAF.NS)
                );
        repository.initialize();

        // enums
        Set<STMT> added = new HashSet<STMT>();
        UID ntType = new UID(TEST.NS, NoteType.class.getSimpleName());
        for (NoteType nt : NoteType.values()) {
            UID ntId = new UID(TEST.NS, nt.name());
            added.add(new STMT(ntId, RDF.type, ntType));
            added.add(new STMT(ntId, CORE.enumOrdinal, new LIT(String.valueOf(nt.ordinal()), XSD.integerType)));
        }
        RDFConnection connection = repository.openConnection();
        connection.update(Collections.<STMT> emptySet(), added);
        connection.close();
    }

    @AfterClass
    public static void after() {
        try {
            if (sessionFactory != null)
                sessionFactory.close();
            if (repository != null)
                repository.close();
        } finally {
            sessionFactory = null;
            repository = null;
        }
    }

    @After
    public void tearDown() throws IOException {
        for (Session s : openSessions) {
            s.close();
        }
        System.out.println();
    }

}
