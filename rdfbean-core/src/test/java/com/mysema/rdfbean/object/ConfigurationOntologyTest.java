/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.ontology.Ontology;


/**
 * ConfigurationTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ConfigurationOntologyTest {
    
    @ClassMapping(ns=TEST.NS)
    public class Entity1{
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Entity2 extends Entity1{
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Entity3 extends Entity2{
        
    }
    
    private Configuration configuration;
    
    private Ontology<UID> ontology;
    
    @Before
    public void setUp(){
        configuration = new DefaultConfiguration(Entity1.class, Entity2.class, Entity3.class);
        ontology = new ConfigurationOntology(configuration);
    }
    
    @Test
    public void getMappedSubtypes(){
        MappedClass cl = configuration.getMappedClass(Entity1.class);
        assertEquals(getUIDs(Entity1.class, Entity2.class, Entity3.class), ontology.getSubtypes(cl.getUID()));
        
        cl = configuration.getMappedClass(Entity2.class);
        assertEquals(getUIDs(Entity2.class, Entity3.class), ontology.getSubtypes(cl.getUID()));
        
        cl = configuration.getMappedClass(Entity3.class);
        assertEquals(getUIDs(Entity3.class), ontology.getSubtypes(cl.getUID()));
    }
    
    @Test
    public void getMappedSupertypes(){
        MappedClass cl = configuration.getMappedClass(Entity3.class);
        assertEquals(getUIDs(Entity1.class, Entity2.class), ontology.getSupertypes(cl.getUID()));
        
        cl = configuration.getMappedClass(Entity2.class);
        assertEquals(getUIDs(Entity1.class), ontology.getSupertypes(cl.getUID()));
        
        cl = configuration.getMappedClass(Entity1.class);
        assertEquals(getUIDs(), ontology.getSupertypes(cl.getUID()));
    }

    private Set<UID> getUIDs(Class<?>... classes){
        Set<UID> uids = new HashSet<UID>();
        for (Class<?> cl : classes){
            uids.add(configuration.getMappedClass(cl).getUID());
        }
        return uids;
    }
}
