/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.mysema.query.Projectable;
import com.mysema.query.Query;
import com.mysema.query.types.Expression;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.TypedList;

public class ConfigurationTest {

    @ClassMapping
    public class Entity {

    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidPackage(){
        new DefaultConfiguration(ConfigurationTest.class.getPackage());
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidClass(){
        new DefaultConfiguration(ConfigurationTest.class);
    }

    @Test
    public void ScanPackages() throws IOException, ClassNotFoundException{
        DefaultConfiguration conf1 = new DefaultConfiguration();
        conf1.addPackages(OWL.class.getPackage());
        conf1.addClasses(TypedList.class);

        DefaultConfiguration conf2 = new DefaultConfiguration();
        conf2.scanPackages(OWL.class.getPackage());
        assertEquals(conf1.getMappedClasses(), conf2.getMappedClasses());
    }

    @Test
    public void ScanPackages_from_File() throws IOException, ClassNotFoundException{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        DefaultConfiguration conf = new DefaultConfiguration();
        Set<Class<?>> classes = conf.scanPackage(classLoader, CORE.class.getPackage());
        assertFalse(classes.isEmpty());
        assertTrue(classes.contains(OWL.class));
    }

    @Test
    public void ScanPackages_from_Jar() throws IOException, ClassNotFoundException{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        DefaultConfiguration conf = new DefaultConfiguration();
        Set<Class<?>> classes = conf.scanPackage(classLoader, Query.class.getPackage());
        assertTrue(classes.contains(Projectable.class));
        assertTrue(classes.contains(Expression.class));
        assertFalse(classes.isEmpty());
    }

    @Test
    public void Default_Namespace_Available(){
        Configuration configuration = new DefaultConfiguration(TEST.NS, Entity.class);
        MappedClass mappedClass = configuration.getMappedClass(Entity.class);
        assertEquals(new UID(TEST.NS, Entity.class.getSimpleName()), mappedClass.getUID());
    }

    @Test(expected=IllegalArgumentException.class)
    public void Default_Namespace_Missing(){
        Configuration configuration = new DefaultConfiguration(Entity.class);
        configuration.getMappedClass(Entity.class);
    }
    
    @Test
    public void GetMappedClasses_For_Unknown(){
        Configuration configuration = new DefaultConfiguration();
        assertEquals(Collections.emptyList(), configuration.getMappedClasses(new UID(TEST.NS)));
    }

}
