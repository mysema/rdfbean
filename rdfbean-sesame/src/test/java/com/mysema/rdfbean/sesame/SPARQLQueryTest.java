package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.Entity;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.EntityRevision;
import com.mysema.rdfbean.domains.EntityRevisionTermDomain.Term;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Entity.class, EntityRevision.class, Term.class})
public class SPARQLQueryTest extends SessionTestBase implements EntityRevisionTermDomain{
    
    @Before
    public void setUp(){
        Entity entity = new Entity();
        EntityRevision revision = new EntityRevision();
        Term term = new Term();
        session.saveAll(entity, revision, term);
        session.flush();
    }
    
    @Test
    public void Select(){        
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s ?p ?o WHERE {?s ?p ?o}");
        assertEquals(SPARQLQuery.ResultType.TUPLES, query.getResultType());
        assertEquals(Arrays.asList("s","p","o"), query.getVariables());
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        assertTrue(rows.hasNext());
        while (rows.hasNext()){
            Map<String,NODE> row = rows.next();
            System.out.println(row.get("s") + " " + row.get("p") + " " + row.get("o"));
        }
    }
    
    @Test
    public void Construct(){        
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o}");
        assertEquals(SPARQLQuery.ResultType.TRIPLES, query.getResultType());
        CloseableIterator<STMT> triples = query.getTriples();
        assertTrue(triples.hasNext());
        while (triples.hasNext()){
            STMT triple = triples.next();
            System.out.println(triple);
        }
    }
    
    @Test
    public void Select_and_Describe(){
        SPARQLQuery query = session.createQuery(QueryLanguage.SPARQL, "SELECT ?s WHERE {?s ?p ?o}");
        CloseableIterator<Map<String,NODE>> rows = query.getTuples();
        assertEquals(Arrays.asList("s"), query.getVariables());
        assertTrue(rows.hasNext());
        while (rows.hasNext()){
            Map<String,NODE> row = rows.next();
            NODE subject = row.get("s");
            if (subject.isURI()){
                SPARQLQuery describe = session.createQuery(QueryLanguage.SPARQL, "DESCRIBE <" + subject.getValue() + ">");
                CloseableIterator<STMT> triples = describe.getTriples();
                assertTrue(triples.hasNext());
                while (triples.hasNext()){
                    STMT triple = triples.next();
                    System.out.println(triple);
                }
            }
        }
    }
                                      

}
