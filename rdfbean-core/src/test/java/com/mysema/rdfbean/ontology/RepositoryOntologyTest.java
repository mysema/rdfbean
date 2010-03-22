/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.ontology;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.ontology.Ontology;
import com.mysema.rdfbean.ontology.RepositoryOntology;
import com.mysema.rdfbean.owl.OWL;

/**
 * RepositoryOntologyTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RepositoryOntologyTest {
        
    @Test
    public void test() throws IOException{
        MiniRepository repository = new MiniRepository();
        UID type1 = new UID(TEST.NS,"Type1");
        UID type2 = new UID(TEST.NS,"Type2");
        UID type3 = new UID(TEST.NS,"Type3");
        
        repository.add(new STMT(type1, RDF.type, OWL.Class));
        repository.add(new STMT(type2, RDF.type, OWL.Class));
        repository.add(new STMT(type3, RDF.type, OWL.Class));        
        repository.add(new STMT(type2, RDFS.subClassOf, type1));
        repository.add(new STMT(type3, RDFS.subClassOf, type2));
                
        Ontology ontology = new RepositoryOntology(repository);
        assertEquals(asSet(type1, type2, type3), ontology.getSubtypes(type1));
        assertEquals(asSet(type2, type3), ontology.getSubtypes(type2));
        assertEquals(asSet(type3), ontology.getSubtypes(type3));
        
        assertEquals(asSet(), ontology.getSupertypes(type1));
        assertEquals(asSet(type1), ontology.getSupertypes(type2));
        assertEquals(asSet(type1, type2), ontology.getSupertypes(type3));
    }
    
    private static Set<UID> asSet(UID... uids){
        return new HashSet<UID>(Arrays.asList(uids));
    }

}
