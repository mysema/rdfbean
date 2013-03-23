package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.Entity;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.EntityRevision;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.Term;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ Entity.class, EntityRevision.class, Term.class })
public class SPARQLQueryTest extends SessionTestBase implements EntityRevisionTermDomain {

    @Before
    public void setUp() {
        Entity entity = new Entity();
        EntityRevision revision = new EntityRevision();
        Term term = new Term();
        session.saveAll(entity, revision, term);
        session.flush();
    }

    @Test
    public void Ask() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "ASK { ?s ?p ?o }");
        assertEquals(SPARQLQuery.ResultType.BOOLEAN, query.getResultType());
        assertTrue(query.getBoolean());
    }

    @Test
    public void Ask_with_False_result() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "ASK { ?s <test:test> ?o }");
        assertEquals(SPARQLQuery.ResultType.BOOLEAN, query.getResultType());
        assertFalse(query.getBoolean());
    }

    @Test
    public void Select() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s ?p ?o WHERE {?s ?p ?o}");
        assertEquals(SPARQLQuery.ResultType.TUPLES, query.getResultType());
        assertEquals(Arrays.asList("s", "p", "o"), query.getVariables());
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()) {
            Map<String, NODE> row = rows.next();
            System.out.println(row.get("s") + " " + row.get("p") + " " + row.get("o"));
        }
        rows.close();
    }

    @Test
    public void Select_with_QueryTime() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s ?p ?o WHERE {?s ?p ?o}");
        query.setMaxQueryTime(1);
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()) {
            rows.next();
        }
        rows.close();
    }

    @Test
    public void Select_with_Bindings() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s ?p ?o WHERE {?s ?p ?o}");
        query.setBinding("p", RDF.type);
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()) {
            Map<String, NODE> row = rows.next();
            assertEquals(RDF.type, row.get("p"));
        }
        rows.close();
    }

    @Test
    public void Select_with_Bindings_no_Match() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 10");
        query.setBinding("p", new UID(TEST.NS, "p" + System.currentTimeMillis()));
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        try {
            assertFalse(rows.hasNext());
        } finally {
            rows.close();
        }
    }

    @Test
    public void Select_with_Bindings_in_Projection() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s ?type ?o WHERE {?s ?p ?o}");
        query.setBinding("type", RDF.type);
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()) {
            Map<String, NODE> row = rows.next();
            assertEquals(RDF.type, row.get("type"));
        }
        rows.close();
    }

    @Test
    public void Construct() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o}");
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        CloseableIterator<STMT> triples = query.getTriples();
        assertTrue(triples.hasNext());
        while (triples.hasNext()) {
            STMT triple = triples.next();
            System.out.println(triple);
        }
        triples.close();
    }

    @Test
    public void Construct_Multiple_Patterns() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "CONSTRUCT { ?s ?p ?o . ?s ?p2 ?type } " +
                "WHERE {?s ?p ?o . ?s ?p2 ?type } LIMIT 10");
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        CloseableIterator<STMT> triples = query.getTriples();
        assertTrue(triples.hasNext());
        while (triples.hasNext()) {
            STMT triple = triples.next();
            System.out.println(triple);
        }
        triples.close();
    }

    @Test
    public void Construct_Stream_Triples() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o}");
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        StringWriter w = new StringWriter();
        query.streamTriples(w, Format.RDFXML.getMimetype());
        assertTrue(w.toString().contains("rdf:RDF"));
    }

    @Test
    public void Select_and_Describe() {
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s WHERE {?s ?p ?o}");
        CloseableIterator<Map<String, NODE>> rows = query.getTuples();
        assertEquals(Arrays.asList("s"), query.getVariables());
        assertTrue(rows.hasNext());
        while (rows.hasNext()) {
            Map<String, NODE> row = rows.next();
            NODE subject = row.get("s");
            if (subject.isURI()) {
                SPARQLQuery describe = session.createQuery(QueryLanguage.SPARQL, "DESCRIBE <" + subject.getValue() + ">");
                CloseableIterator<STMT> triples = describe.getTriples();
                assertTrue(triples.hasNext());
                while (triples.hasNext()) {
                    STMT triple = triples.next();
                    System.out.println(triple);
                }
                triples.close();
            }
        }
    }

}
