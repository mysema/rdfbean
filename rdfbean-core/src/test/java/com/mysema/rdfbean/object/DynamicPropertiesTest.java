/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.l10n.support.LocaleIterable;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.annotations.Properties;
import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * @author mala
 * 
 */
public class DynamicPropertiesTest {
    
    // TODO Marko : add also inverse mapped predicates into the examples

    private static final String CREATOR_COMMENT = "Created under stress";

    private static final String DESCRIPTION1 = "Some description 1";
    
    private static final String DESCRIPTION2 = "Some description 2";
    
    private static final LocalDate CREATED = new LocalDate();
    
    private static final LocalDate DEADLINE = CREATED.plusDays(1);
    
    private interface UIDS {
        UID project = new UID(TEST.NS, "1");
        UID person = new UID(TEST.NS, "2");
        UID owner = new UID(TEST.NS, "owner");
        UID name = new UID(TEST.NS, "name");
        UID created = new UID(TEST.NS, "created");
        UID description = new UID(TEST.NS, "description");
        UID creatorComment = new UID(TEST.NS, "creatorComment");
        UID deadline = new UID(TEST.NS, "deadline");
    }
    
    @ClassMapping(ns = TEST.NS)
    public static class Person {
        
        @Id
        String id;
        
        @Predicate
        String name;
    }
    
    @ClassMapping(ns = TEST.NS)
    public static class Iteration {
        
        @Id
        String id;
        
        @Predicate
        String name;
    }
    
    @ClassMapping(ns = TEST.NS)
    public static class Project {

        @Id
        String id;

        @Predicate
        String name;

        @Predicate
        LocalDate created;
        
        @Properties(includeMapped=true)
        Map<UID, LocalDate> dates;
        
        @Properties
        Map<UID, Person> participants;
        
        @Properties
        Map<UID, Iteration> iterations;

        @Properties
        Map<UID, Set<String>> infos;
        
        public Project() {
        }

        public Project(String name) {
            this.name = name;
        }
    }

    @ClassMapping(ns = TEST.NS)
    public static class InvalidProject1 {

        @Properties
        Map<UID, NODE> starter;

        @Properties
        Map<UID, NODE> invalid;
        // Käsitellään write-case myöhemmin
        // ehkä käytetään read/write booleania
    }

    @ClassMapping(ns = TEST.NS)
    public static class InvalidProject2 {
        @Properties
        List<UID> nodes;
    }

    @ClassMapping(ns = TEST.NS)
    public static class InvalidProject3 {
        @Properties
        Map<String, String> nodes;
    }

    private MiniRepository repository;

    private Session session;

    private SessionFactoryImpl sessionFactory;

    private Locale locale;

    @Before
    public void init() {

        repository = new MiniRepository();
        sessionFactory = new SessionFactoryImpl() {
            @Override
            public Iterable<Locale> getLocales() {
                return new LocaleIterable(locale, false);
            }

        };
        sessionFactory.setRepository(repository);
        DefaultConfiguration configuration = new DefaultConfiguration(
                Project.class, Person.class);
        configuration.setFetchStrategies(Collections
                .<FetchStrategy> emptyList());
        sessionFactory.setConfiguration(configuration);
        sessionFactory.initialize();

        // Basic data
        
        repository.add(
                
                new STMT(UIDS.project, RDF.type,  new UID(TEST.NS, "Project")), 
                new STMT(UIDS.project,  UIDS.name, new LIT("TestProject")),
                new STMT(UIDS.project, UIDS.created, new LIT(CREATED.toString(), XSD.date)),
                
                new STMT(UIDS.person, RDF.type, new UID(TEST.NS, "Person")),
                new STMT(UIDS.person,  UIDS.name, new LIT("Foo Bar"))
        );             
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
    public void testDynamicPropertyRead() {
        
        // Checking preconditions 
        
        newSession();
        Project project = session.get(Project.class, UIDS.project);
        assertEquals("TestProject", project.name);
        
        // FIXME Contains http://semantics.mysema.com/core#localId.
        // What to do about it
        assertEquals(1, project.infos.size());
        
        assertEquals(1, project.dates.size());
        assertTrue(project.dates.containsKey(UIDS.created));
        assertEquals(0, project.participants.size());

        // Adding dynamic data
        
        newSession();

        repository.add(
               new STMT(UIDS.project, UIDS.owner, UIDS.person),
               new STMT(UIDS.project, UIDS.deadline, new LIT(DEADLINE.toString(), XSD.date)),
               new STMT(UIDS.project, UIDS.description, new LIT(DESCRIPTION1)),
               new STMT(UIDS.project, UIDS.description, new LIT(DESCRIPTION2)),
               new STMT(UIDS.project, UIDS.creatorComment, new LIT(CREATOR_COMMENT))
        );

        // Checking dynamic data
        
        project = session.get(Project.class, UIDS.project);
        Person person = session.get(Person.class, UIDS.person);
        assertEquals("Foo Bar", person.name);

        // FIXME Contains http://semantics.mysema.com/core#localId.
        // What to do about it
        assertEquals(3, project.infos.size());
        
        assertEquals(2, project.dates.size());
        
        assertTrue(project.infos.containsKey(UIDS.description));
        assertTrue(project.infos.containsKey(UIDS.creatorComment));
        assertTrue(project.dates.containsKey(UIDS.created));
        assertTrue(project.dates.containsKey(UIDS.deadline));
        assertTrue(project.participants.containsKey(UIDS.owner));

        assertFalse(project.infos.containsKey(UIDS.name));
        
        assertEquals(person, project.participants.get(UIDS.owner));
        assertTrue(project.infos.get(UIDS.description).contains(DESCRIPTION1));
        assertTrue(project.infos.get(UIDS.description).contains(DESCRIPTION2));
        assertTrue(project.infos.get(UIDS.creatorComment).contains(CREATOR_COMMENT));
        assertEquals(CREATED, project.dates.get(UIDS.created));
        assertEquals(DEADLINE, project.dates.get(UIDS.deadline));
    }

    @Test
    public void testMappedClass() throws SecurityException,
            NoSuchFieldException {

        Field field = Project.class.getDeclaredField("infos");

        assertNotNull(field);

        MappedClass mappedClass = MappedClassFactory
                .getMappedClass(Project.class);
        boolean containsInfos = false;

        for (MappedProperty<?> property : mappedClass.getDynamicProperties()) {
            if (property.isDynamic() && property.getName().equals("infos")) {
                containsInfos = true;
                assertEquals(UID.class, property.getKeyType());
                assertTrue(Set.class.isAssignableFrom(property.getDynamicCollectionType()));
                assertEquals(String.class, property.getDynamicCollectionComponentType());
            }
        }

        assertTrue("Could not find property 'infos'", containsInfos);
    }

    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProject1() {
        // TODO How to handle this case
        MappedClassFactory.getMappedClass(InvalidProject1.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProject2() {
        MappedClassFactory.getMappedClass(InvalidProject2.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProject3() {
        MappedClassFactory.getMappedClass(InvalidProject3.class);
    }
}