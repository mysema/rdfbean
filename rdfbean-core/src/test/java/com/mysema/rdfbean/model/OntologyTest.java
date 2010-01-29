/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.Ontology;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.DefaultOntology;
import com.mysema.rdfbean.object.MappedClass;


/**
 * ConfigurationTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OntologyTest {
    
    @ClassMapping(ns=TEST.NS)
    public class Entity1{
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Entity2 extends Entity1{
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Entity3 extends Entity2{
        
    }
    
    private Ontology ontology;
    
    @Before
    public void setUp(){
        Configuration configuration = new DefaultConfiguration(Entity1.class, Entity2.class, Entity3.class);
        ontology = new DefaultOntology(configuration);
    }
    
    @Test
    public void getMappedSubtypes(){
        MappedClass cl = MappedClass.getMappedClass(Entity1.class);
        assertEquals(getUIDs(Entity1.class, Entity2.class, Entity3.class), ontology.getSubtypes(cl.getUID()));
        
        cl = MappedClass.getMappedClass(Entity2.class);
        assertEquals(getUIDs(Entity2.class, Entity3.class), ontology.getSubtypes(cl.getUID()));
        
        cl = MappedClass.getMappedClass(Entity3.class);
        assertEquals(getUIDs(Entity3.class), ontology.getSubtypes(cl.getUID()));
    }
    
    @Test
    public void getMappedSupertypes(){
        MappedClass cl = MappedClass.getMappedClass(Entity3.class);
        assertEquals(getUIDs(Entity1.class, Entity2.class, Entity3.class), ontology.getSupertypes(cl.getUID()));
        
        cl = MappedClass.getMappedClass(Entity2.class);
        assertEquals(getUIDs(Entity1.class, Entity2.class), ontology.getSupertypes(cl.getUID()));
        
        cl = MappedClass.getMappedClass(Entity1.class);
        assertEquals(getUIDs(Entity1.class), ontology.getSupertypes(cl.getUID()));
    }

    private static Set<UID> getUIDs(Class<?>... classes){
        Set<UID> uids = new HashSet<UID>();
        for (Class<?> cl : classes){
            uids.add(MappedClass.getMappedClass(cl).getUID());
        }
        return uids;
    }
}
