package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.NoteTypeDomain.NoteType;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.testutil.SessionRule;

public abstract class AbstractConnectionTest {

    protected static VirtuosoRepository repository;

    protected static final UID context = new UID(TEST.NS, "named1");

    protected static final UID context2 = new UID(TEST.NS, "named2");

    protected static final UID example = new UID("http://example.com");

    protected static final UID ex1 = new UID("http://ex1.com");

    protected static final UID ex2 = new UID("http://ex2.com");

    protected VirtuosoRepositoryConnection connection;

    protected Collection<STMT> toBeRemoved;

    @Rule
    public SessionRule sessionRule = new SessionRule(repository);

    public Session session;

    @BeforeClass
    public static void setUpClass() {
        repository = new VirtuosoRepository("localhost:1111", "dba", "dba", TEST.NS);
        repository.setAllowedGraphs(Arrays.asList(context, context2, example, ex1, ex2));
        repository.initialize();

        // enums
        Set<STMT> added = new HashSet<STMT>();
        for (NoteType nt : NoteType.values()) {
            added.add(new STMT(
                    new UID(TEST.NS, nt.name()),
                    CORE.enumOrdinal,
                    new LIT(String.valueOf(nt.ordinal()), XSD.integerType)));
        }

        RDFConnection conn = repository.openConnection();
        try {
            conn.update(Collections.<STMT> emptySet(), added);
        } finally {
            conn.close();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        repository.close();
    }

    @Before
    public void setUp() {
        connection = repository.openConnection();
    }

    @After
    public void tearDown() {
        if (connection != null) {
            if (toBeRemoved != null) {
                connection.update(toBeRemoved, null);
            }
            connection.close();
        }
    }

    protected void assertExists(STMT stmt) {
        assertExists(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext()); // s
                                                                                                   // p
                                                                                                   // o
        assertExists(stmt.getSubject(), stmt.getPredicate(), null, stmt.getContext()); // s
                                                                                       // p
                                                                                       // -
        assertExists(stmt.getSubject(), null, null, stmt.getContext()); // s - -
        assertExists(null, stmt.getPredicate(), stmt.getObject(), stmt.getContext()); // -
                                                                                      // p
                                                                                      // o
        assertExists(null, null, stmt.getObject(), stmt.getContext()); // - - o
    }

    protected List<STMT> findStatements(ID subject, UID predicate, NODE object, UID context) {
        return IteratorAdapter.asList(connection.findStatements(subject, predicate, object, context, false));
    }

    protected void assertExists(ID subject, UID predicate, NODE object, UID context) {
        assertTrue(connection.exists(subject, predicate, object, context, false));
    }

    protected void assertNotExists(ID subject, UID predicate, NODE object, UID context) {
        assertFalse(connection.exists(subject, predicate, object, context, false));
    }

}
