/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
public class LuceneRepositoryTest extends AbstractRepositoryTest{
        
    UID bobRes = new UID(TEST.NS, "bob");
    UID anneRes = new UID(TEST.NS, "anne");
    UID personType = new UID(TEST.NS, "Person");
    UID friendPred = new UID(TEST.NS, "friend");
        
    @Test
    public void test() throws IOException{
        LuceneConnection connection = luceneRepository.openConnection();
        
        // tx #1
        RDFBeanTransaction tx = connection.beginTransaction(null, false, 0, 0);
        List<STMT> added = Arrays.asList(
                // bob
                new STMT(bobRes, RDF.type, personType),
                new STMT(bobRes, RDFS.label, new LIT("Bobby")),
                new STMT(bobRes, RDFS.comment, new LIT("Bobby is a nice guy")),
                new STMT(bobRes, friendPred, anneRes),
                    
                // anne
                new STMT(anneRes, RDF.type, personType),
                new STMT(anneRes, RDFS.label, new LIT("Anne")),
                new STMT(anneRes, RDFS.comment, new LIT("Anne is a nice girl"))
                    
                // ...
            );
            connection.update(Collections.<STMT>emptySet(), new HashSet<STMT>(added));
        tx.commit();
//        connection.close();
        
        // tx #2
//      connection = luceneRepository.openConnection();
        tx = connection.beginTransaction(null, true, 0, 0);
        assertEquals(1, connection.countDocuments(bobRes, null,     null,       null));
        assertEquals(2, connection.countDocuments(null,   null,     personType, null));
//        assertEquals(2, connection.countDocuments(null,   RDF.type, null,       null));
//        assertEquals(2, connection.countDocuments(null,   RDF.type, personType, null));
        tx.commit();
        connection.close();
    }

}
