package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.owl.OWL;

public class VirtuosoRepositoryConnectionTest extends AbstractConnectionTest{

    @Test
    public void IsAllowedGraph(){
        VirtuosoRepositoryConnection conn = new VirtuosoRepositoryConnection(null, null, 0, new UID(TEST.NS), Collections.<UID>emptySet(), null);
        assertFalse(conn.isAllowedGraph(new UID(OWL.NS)));
        assertFalse(conn.isAllowedGraph(new UID(RDF.NS)));
        assertFalse(conn.isAllowedGraph(new UID(RDFS.NS)));
        assertFalse(conn.isAllowedGraph(new UID("http://localhost:8890/DAV")));

        assertTrue(conn.isAllowedGraph(new UID(TEST.NS, "test")));
    }

    @Test
    public void Exists() {
        ID sub = new UID(TEST.NS, "e"+ System.currentTimeMillis());
        ID type = new UID(TEST.NS, "TestType" + System.currentTimeMillis());
        assertNotExists(sub, null, null, null);
        repository.execute(new Addition(new STMT(sub, RDF.type, type)));
        toBeRemoved = Collections.singleton(new STMT(sub, RDF.type, type));

        assertExists(new STMT(sub, RDF.type, type));
    }

    @Test
    public void FindStatements() {
        ID sub = new UID(TEST.NS, UUID.randomUUID().toString());
        assertTrue(IteratorAdapter.asList(connection.findStatements(sub, null, null, null, false)).isEmpty());
        repository.execute(new Addition(new STMT(sub, RDF.type, RDFS.Class)));
        toBeRemoved = Collections.singleton(new STMT(sub, RDF.type, RDFS.Class));

        assertFalse(findStatements(sub,  null,     null,       null).isEmpty());
        assertFalse(findStatements(sub,  RDF.type, null,       null).isEmpty());
        assertFalse(findStatements(null, RDF.type, RDFS.Class, null).isEmpty());
    }

    @Test
    public void FindStatements_from_Context() {
        UID sub = new UID(TEST.NS, UUID.randomUUID().toString());
        assertTrue(findStatements(sub, null, null, null).isEmpty());
        repository.execute(new Addition(new STMT(sub, RDF.type, RDFS.Class, context)));
        toBeRemoved = Collections.singleton(new STMT(sub, RDF.type, RDFS.Class, context));

        assertFalse(findStatements(sub,  null, null, null).isEmpty());
        assertFalse(findStatements(sub,  null, null, context).isEmpty());
        assertFalse(findStatements(null, null, null, context).isEmpty());
    }

    @Test
    public void Resources(){
        ID sub = new UID(TEST.NS, "e"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(new STMT(sub, RDFS.label, sub));
        toBeRemoved = stmts;
        connection.update(null, stmts);

        List<STMT> found = findStatements(sub, null, null, null);
        assertEquals(new HashSet<STMT>(stmts), new HashSet<STMT>(found));
        assertExists(stmts.get(0));
    }

    @Test
    public void AddBulk() throws SQLException, IOException{
        ID sub = new UID(TEST.NS, "e"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(
                // uid
                new STMT(sub, RDFS.label, sub),
                // bid
//                new STMT(sub, RDFS.label, new BID()),
                // lit
                new STMT(sub, RDFS.label, new LIT(sub.getId())),
                new STMT(sub, RDFS.label, new LIT("X")),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), Locale.ENGLISH)),
                new STMT(sub, RDFS.label, new LIT("1", XSD.intType))
                );
        toBeRemoved = stmts;

        connection.addBulk(stmts);

        for (STMT stmt : stmts){
            assertExists(stmt);
        }
    }

    @Test
    public void AddBulk_with_BlankNodes() throws SQLException, IOException{
        List<STMT> stmts = Collections.singletonList(new STMT(new BID(), RDF.type, new BID()));
        toBeRemoved = stmts;
        connection.addBulk(stmts);

        assertExists(stmts.get(0));
        assertTrue(findStatements(stmts.get(0).getSubject(), null, null, null).containsAll(stmts));
        assertTrue(findStatements(null, null, stmts.get(0).getObject(), null).containsAll(stmts));
    }


    @Test
    public void Literals(){
        ID sub = new UID(TEST.NS, "e"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, RDFS.label, new LIT(sub.getId())),
                new STMT(sub, RDFS.label, new LIT("X")),
                new STMT(sub, RDFS.label, new LIT("äö")),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), Locale.ENGLISH)),
                new STMT(sub, RDFS.label, new LIT("1", XSD.intType))
                );
        toBeRemoved = stmts;
        connection.update(null, stmts);

        List<STMT> found = findStatements(sub, null, null, null);
        assertEquals(new HashSet<STMT>(stmts), new HashSet<STMT>(found));

        // find int literal
        assertExists(new STMT(sub, RDFS.label, new LIT("1", XSD.intType)));
        // find string literal
        assertExists(new STMT(sub, RDFS.label, new LIT("X")));
        // find string literal
        assertExists(new STMT(sub, RDFS.label, new LIT(sub.getId())));
    }

    @Test
    public void BlankNodes(){
        ID sub = new BID();
        ID obj = new BID();
        List<STMT> stmts = Collections.singletonList(new STMT(sub, RDF.type, obj));
        toBeRemoved = stmts;
        connection.update(null, stmts);

        List<STMT> found = findStatements(sub, null, null, null);
        assertEquals(new HashSet<STMT>(stmts), new HashSet<STMT>(found));

        assertExists(stmts.get(0));
    }

    @Test
    public void Remove_subject_and_object_given(){
        // FIXME: this is too slow
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(stmt.getSubject(), null, null, null);
        connection.remove(sub, null, obj, null);
        assertNotExists(stmt.getSubject(), null, null, null);
    }

    @Test
    public void Remove_subject_and_literal_object_given(){
        // FIXME: this is too slow
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        NODE obj = new LIT(TEST.NS + "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(stmt.getSubject(), null, null, null);
        connection.remove(sub, null, obj, null);
        assertNotExists(stmt.getSubject(), null, null, null);
    }

    @Test
    public void Remove_subject_and_predicate_given(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, RDF.type, obj),
                new STMT(sub, RDFS.label, new LIT("X"))
        );
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(sub, null, null, null);
        connection.remove(sub, RDF.type, null, null);
        assertNotExists(sub, RDF.type, null, null);
        assertExists(sub, RDFS.label, null, null);
    }

    @Test
    public void Remove_none_given_from_default(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(stmt.getSubject(), null, null, null);
        connection.remove(null, null, null, null);
        assertNotExists(stmt.getSubject(), null, null, null);
    }

    @Test
    public void Remove_none_given_from_named(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj, context);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(stmt.getSubject(), null, null, null);
        connection.remove(null, null, null, null);
        assertNotExists(stmt.getSubject(), null, null, null);
    }

    @Test
    public void Remove_all_given_from_default(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(stmt.getSubject(), null, null, null);
        connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
        assertNotExists(stmt.getSubject(), null, null, null);
    }

    @Test
    public void Remove_all_given_from_named(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj, context);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(stmt.getSubject(), null, null, null);
        connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
        assertNotExists(stmt.getSubject(), null, null, null);
    }

    @Test
    public void Remove_subject_given(){
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID obj = new UID(TEST.NS, "o"+ System.currentTimeMillis());
        STMT stmt = new STMT(sub, RDF.type, obj);
        List<STMT> stmts = Collections.singletonList(stmt);
        toBeRemoved = stmts;
        connection.update(null, stmts);

        assertExists(stmt.getSubject(), null, null, null);
        connection.remove(stmt.getSubject(), null, null, null);
        assertNotExists(stmt.getSubject(), null, null, null);
    }

    @Test
    public void Remove_Literals_from_named(){
        ID sub = new UID(TEST.NS, "el"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, RDFS.label, new LIT(sub.getId(), new UID(TEST.NS)), context),
                new STMT(sub, RDFS.label, new LIT(sub.getId()), context),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), Locale.ENGLISH), context),
                new STMT(sub, RDFS.label, new LIT("1", XSD.intType), context),
                new STMT(sub, RDFS.label, new LIT("20011010", XSD.date), context)
                );
        toBeRemoved = stmts;
        connection.update(null, stmts);

        for (STMT stmt : stmts){
            connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
            assertFalse(stmt.toString(), connection.exists(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext(), false));
        }
        assertNotExists(sub, null, null, null);
    }

    @Test
    public void Remove_Literals_from_default(){
        ID sub = new UID(TEST.NS, "el"+ System.currentTimeMillis());
        List<STMT> stmts = Arrays.asList(
                new STMT(sub, new UID(TEST.NS), new LIT("x", new UID(TEST.NS))),
                new STMT(sub, new UID(TEST.NS), new LIT("1", new UID(TEST.NS))),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), new UID(TEST.NS))),
                new STMT(sub, RDFS.label, new LIT(sub.getId())),
                new STMT(sub, RDFS.label, new LIT(sub.getId(), Locale.ENGLISH)),
                new STMT(sub, RDFS.label, new LIT("1", XSD.intType)),
                new STMT(sub, new UID(TEST.NS), new LIT("1", XSD.intType)),
                new STMT(sub, RDFS.label, new LIT("20011010", XSD.date)),
                new STMT(sub, new UID(TEST.NS), new LIT("20011010", XSD.date))
                );
        toBeRemoved = stmts;
        connection.update(null, stmts);

        for (STMT stmt : stmts){
            connection.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext());
            assertFalse(stmt.toString(), connection.exists(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), stmt.getContext(), false));
        }
        for(STMT stmt : findStatements(sub, null, null, null)){
            System.err.println(stmt);
        }
        assertNotExists(sub, null, null, null);
    }

    @Test
    public void Update() {
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID type = new UID(TEST.NS, "TestType" + System.currentTimeMillis());
        Collection<STMT> stmts = Collections.singleton(new STMT(sub, RDF.type, type));
        toBeRemoved = stmts;
        // add
        connection.update(null, stmts);
        assertExists(sub, RDF.type, type, null);
        // remove
        connection.update(stmts, null);
        assertNotExists(sub, RDF.type, type, null);
    }

    @Test
    public void Update_named() {
        ID sub = new UID(TEST.NS, "s"+ System.currentTimeMillis());
        ID type = new UID(TEST.NS, "TestType" + System.currentTimeMillis());
        Collection<STMT> stmts = Collections.singleton(new STMT(sub, RDF.type, type, context));
        toBeRemoved = stmts;
        // add
        connection.update(null, stmts);
        assertExists(sub, RDF.type, type, null);
        // remove
        connection.update(stmts, null);
        assertNotExists(sub, RDF.type, type, null);
    }



    @Test
    public void ConstructOrder(){
        List<STMT> stmts = Arrays.asList(
            new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("a")),
            new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("b")),
            new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("c")),
            new STMT(new UID(TEST.NS, "e1"), RDFS.label, new LIT("d"))
        );
        toBeRemoved = stmts;
        connection.update(null, stmts);

        //asc
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL,
                "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o . FILTER ( ?s = <" + stmts.get(0).getSubject().getId() + "> ) } ORDER BY ?s" );
//        query.setBinding("s", stmts.get(0).getSubject());
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        List<STMT> asc = IteratorAdapter.asList(query.getTriples());
        assertEquals(stmts, asc);

    }
}
