/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.DefaultConfiguration;

/**
 * InheritanceConfTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class InheritanceTest extends AbstractConfigurationTest{
    
    @Test
    public void Inheritance(){
        configuration.setCoreConfiguration(new DefaultConfiguration(Animal.class, Cat.class, Dog.class));
        configuration.initialize();
        
        // name needs to be handled everywhere, since it's declared as a searchable predicate
        // in Animal
        UID namePred = new UID(TEST.NS, "name");
        assertNotNull(configuration.getPropertyConfig(namePred, Collections.singleton(new UID(TEST.NS, Animal.class.getSimpleName()))));
        assertNotNull(configuration.getPropertyConfig(namePred, Collections.singleton(new UID(TEST.NS, Cat.class.getSimpleName()))));
        assertNotNull(configuration.getPropertyConfig(namePred, Collections.singleton(new UID(TEST.NS, Dog.class.getSimpleName()))));
        
        // mate is also processed
        UID matePred = new UID(TEST.NS, "mate");
        assertNotNull(configuration.getPropertyConfig(matePred, Collections.singleton(new UID(TEST.NS, Cat.class.getSimpleName()))));
        
        // age in the Dog context is not processed
        UID agePred = new UID(TEST.NS, "age");
        assertNull(configuration.getPropertyConfig(agePred, Collections.singleton(new UID(TEST.NS, Dog.class.getSimpleName()))));
        
        // age in Animal context is also not processed
        assertNull(configuration.getPropertyConfig(agePred, Collections.singleton(new UID(TEST.NS, Animal.class.getSimpleName()))));
        
    }
    
    @Test
    public void SupertypesMapping(){
        configuration.setCoreConfiguration(new DefaultConfiguration(Animal.class, BlackCat.class, Cat.class, Dog.class));
        configuration.initialize();
        
        // Animal, Cat
        assertEquals(2, configuration.getSupertypes(new UID(TEST.NS, Cat.class.getSimpleName())).size());
    }
    
    @Test
    public void SubtypesMapping(){
        configuration.setCoreConfiguration(new DefaultConfiguration(Animal.class, BlackCat.class, Cat.class, Dog.class));
        configuration.initialize();
        
        // Animal, Cat, BlackCat and Dog
        assertEquals(4, configuration.getSubtypes(new UID(TEST.NS, Animal.class.getSimpleName())).size());
    }
    
    @ClassMapping(ns=TEST.NS)
    @Searchable
    public static class Animal{
        @Predicate
        @SearchablePredicate
        String name;
    }
    
    @ClassMapping(ns=TEST.NS)
    @Searchable
    public static class Cat extends Animal{
        @Predicate
        @SearchablePredicate
        Cat mate;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Dog extends Animal{
        @Predicate
        int age;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class BlackCat extends Cat{
        
    }

}
