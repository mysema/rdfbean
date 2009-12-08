/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.RDFBeanTransaction;

/**
 * LuceneRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneRepositoryTest {
    
    private LuceneRepository luceneRepository;
    
    private int hits;
    
    private Collector collector = new Collector(){
        @Override
        public void collect(int doc) throws IOException {
            hits++;                
        }
        @Override
        public boolean acceptsDocsOutOfOrder() { return true;}            
        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException { }
        @Override
        public void setScorer(Scorer scorer) throws IOException { }            
    };
    
    @Before
    public void setUp(){
        LuceneConfiguration configuration = new LuceneConfiguration();
        configuration.setDirectory(new RAMDirectory());
        luceneRepository = new LuceneRepository(configuration);
    }
    
    @Test
    public void test() throws IOException{
        LuceneConnection connection = luceneRepository.openConnection();
        UID subject = new UID(TEST.NS, "bob");
        
        // tx #1
        RDFBeanTransaction tx = connection.beginTransaction(null, false, 0, 0);
        connection.update(
                Collections.<STMT>emptySet(),
                new HashSet<STMT>(Arrays.asList(
                    new STMT(subject, RDF.type, new UID(TEST.NS, "Person")),
                    new STMT(subject, RDFS.label, new LIT("Bobby")),
                    new STMT(subject, RDFS.comment, new LIT("Bobby is a nice guy"))
                )));
        tx.commit();
        connection.close();
        
        // tx #2
        connection = luceneRepository.openConnection();
        tx = connection.beginTransaction(null, true, 0, 0);
        TermQuery query = new TermQuery(new Term(Constants.ID_FIELD_NAME, subject.getId()));
        connection.search(query, collector);        
        tx.commit();
        connection.close();
        
        assertTrue("Got no hits", hits > 0);
    }

}
