package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;

public class SPARQLQueryTest extends AbstractConnectionTest {

    private static final String CONSTRUCT_LIMIT_10 = "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o} LIMIT 10";
    
    private static final String SELECT_LIMIT_10 = "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 10";

    @Test
    public void Ask(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, "ASK { ?s ?p ?o }");
        assertEquals(SPARQLQuery.ResultType.BOOLEAN, query.getResultType());
        assertTrue(query.getBoolean());
    }

    @Test
    public void Ask_with_False_result(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, "ASK { ?s <test:test> ?o }");
        assertEquals(SPARQLQuery.ResultType.BOOLEAN, query.getResultType());
        assertFalse(query.getBoolean());
    }
    
    @Test
    public void Select(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, SELECT_LIMIT_10);
        assertEquals(SPARQLQuery.ResultType.TUPLES, query.getResultType());
        assertEquals(Arrays.asList("s","p","o"), query.getVariables());
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()){
            Map<String,NODE> row = rows.next();
            System.out.println(row.get("s") + " " + row.get("p") + " " + row.get("o"));
        }
        rows.close();
    }

    @Test
    public void Select_with_QueryTime(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, SELECT_LIMIT_10);
        query.setMaxQueryTime(1);
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()){
            rows.next();
        }
        rows.close();
    }

    @Test
    public void Select_with_Bindings(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, SELECT_LIMIT_10);
        query.setBinding("p", RDF.type);
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()){
            Map<String,NODE> row = rows.next();
            assertEquals(RDF.type, row.get("p"));
        }
        rows.close();
    }
    
    @Test
    public void Select_with_Resource_Binding_no_Match(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, SELECT_LIMIT_10);
        query.setBinding("p", new UID(TEST.NS, "p" + System.currentTimeMillis()));
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        try{
            assertFalse(rows.hasNext());    
        }finally{
            rows.close();
        }
    }
    
    @Test
    public void Select_with_Literal_Binding_no_Match(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, SELECT_LIMIT_10);
        query.setBinding("o", new LIT(UUID.randomUUID().toString()));
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        try{
            assertFalse(rows.hasNext());    
        }finally{
            rows.close();
        }
    }

    @Test
    public void Select_with_Bindings_in_Projection(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, "SELECT ?s ?label ?o WHERE {?s ?p ?o} LIMIT 10");
        query.setBinding("label", RDFS.label);
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()){
            Map<String,NODE> row = rows.next();
            System.out.println(row);
            assertEquals(RDFS.label, row.get("label"));
        }
        rows.close();
    }

    @Test
    public void Construct(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, CONSTRUCT_LIMIT_10);
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        CloseableIterator<STMT> triples = query.getTriples();
        assertTrue(triples.hasNext());
        while (triples.hasNext()){
            STMT triple = triples.next();
            System.out.println(triple);
        }
        triples.close();
    }
    
    @Test
    public void Construct_Multiple_Patterns(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, "CONSTRUCT { ?s ?p ?o . ?s rdf:type ?type } " +
        		"WHERE {?s ?p ?o . ?s rdf:type ?type } LIMIT 10");
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        CloseableIterator<STMT> triples = query.getTriples();
        assertTrue(triples.hasNext());
        while (triples.hasNext()){
            STMT triple = triples.next();
            System.out.println(triple);
        }
        triples.close();
    }


    @Test
    public void Construct_Stream_Triples(){        
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, CONSTRUCT_LIMIT_10);
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        StringWriter w = new StringWriter();
        query.streamTriples(w, Format.RDFXML.getMimetype());
        assertTrue(w.toString().contains("rdf:RDF"));
    }

    @Test
    @Ignore // FIXME
    public void Select_and_Describe(){
        SPARQLQuery query = connection.createQuery(QueryLanguage.SPARQL, "SELECT ?s WHERE {?s ?p ?o} LIMIT 10");
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        assertEquals(Arrays.asList("s"), query.getVariables());
        assertTrue(rows.hasNext());
        while (rows.hasNext()){
            Map<String,NODE> row = rows.next();
            NODE subject = row.get("s");
            if (subject.isURI()){
                SPARQLQuery describe = connection.createQuery(QueryLanguage.SPARQL, "DESCRIBE <" + subject.getValue() + ">");
                CloseableIterator<STMT> triples = describe.getTriples();
                assertTrue(triples.hasNext());
                while (triples.hasNext()){
                    STMT triple = triples.next();
                    System.out.println(triple);
                }
                triples.close();
            }
        }
    }


}
