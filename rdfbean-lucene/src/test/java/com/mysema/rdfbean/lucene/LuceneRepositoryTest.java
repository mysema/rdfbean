/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.owl.OWL;

/**
 * LuceneRepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneRepositoryTest extends AbstractRepositoryTest{
        
    static final List<UID> subjects = Arrays.asList(
            XSD.stringType,
            OWL.Class,
            RDF.Alt,
            RDFS.comment,
            new UID(TEST.NS, "bob"),
            new UID(TEST.NS, "anne"),
            new UID(TEST.NS, "Person"),
            new UID("http://mycompany.com/", "test")            
    );
    
    static final List<UID> predicates = Arrays.asList(
            OWL.allValuesFrom,
            RDF.first,
            RDFS.comment,
            new UID(TEST.NS, "friend")            
    );
    
    static final List<LIT> literals = Arrays.asList(
            new LIT("test"),
            new LIT("test", "en"),
            new LIT("test2", "en"),
            new LIT("test3", "fi"),
            new LIT("test4", new UID(TEST.NS, "custType")),
            new LIT("test5", new UID("http://mycompany.com/", "test"))
    );
    
    static final List<NODE> objects = new ArrayList<NODE>();
    
    static{
        objects.addAll(subjects);
        objects.addAll(literals);
    }
    
    @Test
    public void test() throws IOException{
        LuceneConnection connection = luceneRepository.openConnection();
        
        // tx #1
        RDFBeanTransaction tx = connection.beginTransaction(null, false, 0, 0);
        Set<STMT> added = new HashSet<STMT>();
        for (UID subject : subjects){
            for (UID predicate : predicates){
                for (NODE object : objects){
                    added.add(new STMT(subject, predicate, object));
                }
            }
        }
        connection.update(Collections.<STMT>emptySet(), added);
        tx.commit();
        
        // tx #2
        tx = connection.beginTransaction(null, true, 0, 0);
        assertEquals(subjects.size(), connection.countDocuments(null, null, null, null));        
        
        // documents counts
        for (UID subject : subjects){
            assertEquals(1, connection.countDocuments(subject, null, null, null));
            
            for (UID predicate : predicates){
                assertEquals(1, connection.countDocuments(subject, predicate, null,   null));
                
                for (NODE object : objects){
                    assertEquals(1, connection.countDocuments(subject, predicate, object, null));
                    assertEquals(subjects.size(), connection.countDocuments(null,    predicate, object, null));                                        
                    assertEquals(subjects.size(), connection.countDocuments(null,    null,      object, null));
                }
            }
        }
        
        // statement counts
        
        for (UID subject : subjects){
            assertEquals(predicates.size() * objects.size(), connection.countStatements(subject, null, null, null));
            
            for (UID predicate : predicates){
                assertEquals(objects.size(), connection.countStatements(subject, predicate, null, null));
                
                for (NODE object : objects){
                    assertEquals(1, connection.countStatements(subject, predicate, object, null));
                    assertEquals(subjects.size(), connection.countStatements(null, predicate, object, null));                                        
                    assertEquals(subjects.size() * predicates.size(), connection.countStatements(null, null, object, null));
                }
            }
        }
        
        
        tx.commit();
        connection.close();
    }

}
