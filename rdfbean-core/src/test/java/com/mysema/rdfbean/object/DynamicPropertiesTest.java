/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.l10n.support.LocaleIterable;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.annotations.Properties;
import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
public class DynamicPropertiesTest {

    @ClassMapping(ns=TEST.NS)
    public static class Project {
    
        @Id
        String id;
        
        @Predicate
        String name;
        
        @Properties
        Map<UID, NODE> starter;
        
        public Project() {}
        
        public Project(String name) {
            this.name = name;
        }
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class InvalidProject1 {
    
        @Properties
        Map<UID, NODE> starter;
        
        @Properties
        Map<UID, NODE> invalid;
        // Käsitellään write-case myöhemmin
        // ehkä käytetään read/write booleania
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class InvalidProject2 {
        @Properties
        List<UID> nodes;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class InvalidProject3 {
        @Properties
        Map<String, String> nodes;
    }
    
    private List<LID> ids;
    
    private MiniRepository repository;
    
    private Session session;
    
    private SessionFactoryImpl sessionFactory;

    private Locale locale;

    private UID starterUID =  new UID(TEST.NS, "starter");
    
    @Before
    public void init() {
        Project project = new Project("TestProject");
        
        //http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://semantics.mysema.com/test#Project        
        repository = new MiniRepository();
        sessionFactory = new SessionFactoryImpl() {

            @Override
            public Iterable<Locale> getLocales() {
                return new LocaleIterable(locale, false);
            }
            
        };
        sessionFactory.setRepository(repository);
        DefaultConfiguration configuration = new DefaultConfiguration(Project.class);
        configuration.setFetchStrategies(Collections.<FetchStrategy>emptyList());
        sessionFactory.setConfiguration(configuration);
        sessionFactory.initialize();
        
        repository.add(
                new STMT(new UID(TEST.NS, "1"), new UID(RDF.NS, "type"), new UID(TEST.NS, "Project")),
                new STMT(new UID(TEST.NS, "1"), new UID(TEST.NS, "name"), new LIT("TestProject"))
        );
        
        //ids = newSession().saveAll(project);
    }

    private Session newSession() {
        return newSession(Locale.ENGLISH);
    }

    private Session newSession(Locale locale) {
        this.locale = locale;
        session = sessionFactory.openSession();
        return session;
    }
    
    @Test
    public void testInitialData() {
        newSession();
        Project project = session.get(Project.class, new UID(TEST.NS, "1"));
        assertEquals("TestProject", project.name);
        assertNull(project.starter);
        
        newSession();
        
        repository.add(
                new STMT(new UID(TEST.NS, "1"), starterUID, new LIT("Jepujep"))
        );
        
//        project = session.get(Project.class, new UID(TEST.NS, "1"));
//        
//        assertNotNull(project.starter);
        
        //assertTrue(project.starter.containsKey(starterUID));
    }
    
    @Test
    public void testMappedClass() throws SecurityException, NoSuchFieldException {
        
        Field field = Project.class.getDeclaredField("starter");
        
        assertNotNull(field);
        
        MappedClass mappedClass = MappedClassFactory.getMappedClass(Project.class);
        boolean containsStarter = false;
        
        for (MappedProperties properties : mappedClass.getDynamicProperties()) {
            if (properties.getMappedProperty().getName().equals("starter")) {
                containsStarter = true;
                
                assertEquals(properties.getMappedKey(), field.getGenericType());
                assertEquals(UID.class, properties.getMappedProperty().getKeyType());
                assertEquals(NODE.class, properties.getMappedProperty().getTargetType());
            }
        }
        
        assertTrue("Could not find property 'starter'", containsStarter);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidProject1() {
        MappedClassFactory.getMappedClass(InvalidProject1.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidProject2() {
        MappedClassFactory.getMappedClass(InvalidProject2.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidProject3() {
        MappedClassFactory.getMappedClass(InvalidProject3.class);
    }
}