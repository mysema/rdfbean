package com.mysema.rdfbean.query;

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
import com.mysema.rdfbean.domains.NoteTypeDomain.NoteType;
import com.mysema.rdfbean.domains.SimpleDomain;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.testutil.SessionRule;

public abstract class SessionTestBase implements SimpleDomain {

    protected static MiniRepository repository;

    // private static SessionFactory sessionFactory;

    @Rule
    public SessionRule sessionRule = new SessionRule(repository, false);

    public Session session;

    private final List<Session> openSessions = new ArrayList<Session>();

    @BeforeClass
    public static void beforeClass() throws IOException {
        repository = new MiniRepository();
        repository.initialize();

        // enums
        Set<STMT> added = new HashSet<STMT>();
        for (NoteType nt : NoteType.values()) {
            added.add(new STMT(
                    new UID(TEST.NS, nt.name()),
                    CORE.enumOrdinal,
                    new LIT(String.valueOf(nt.ordinal()), XSD.integerType)));
        }
        RDFConnection connection = repository.openConnection();
        connection.update(Collections.<STMT> emptySet(), added);
        connection.close();
    }

    @AfterClass
    public static void afterClass() {
        try {
            // if (sessionFactory != null) sessionFactory.close();
            if (repository != null)
                repository.close();
        } finally {
            // sessionFactory = null;
            repository = null;
        }
    }

    @After
    public void after() {
        repository.clear();
    }

    @After
    public void tearDown() throws IOException {
        for (Session s : openSessions) {
            s.close();
        }
        System.out.println();
    }

}