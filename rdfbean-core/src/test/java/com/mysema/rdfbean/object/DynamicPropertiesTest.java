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
import java.util.HashMap;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.PropertiesDomain;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * @author mala
 * 
 */
public class DynamicPropertiesTest implements PropertiesDomain{
    
    private static final String CREATOR_COMMENT = "Created under stress";

    private static final String DESCRIPTION1 = "Some description 1";
    
    private static final String DESCRIPTION2 = "Some description 2";
    
    private static final LocalDate CREATED = new LocalDate();
    
    private static final LocalDate DEADLINE = CREATED.plusDays(1);
    
    private DefaultConfiguration configuration;
    
    private MiniRepository repository;

    private Session session;

    private SessionFactoryImpl sessionFactory;

    @Before
    public void setUp() {
        repository = new MiniRepository();
        
        configuration = new DefaultConfiguration(Project.class, Person.class);
        
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setRepository(repository);
        sessionFactory.setConfiguration(configuration);
        sessionFactory.initialize();
        
        repository.add(                
                new STMT(_project, RDF.type,  new UID(TEST.NS, "Project")), 
                new STMT(_project,  _name, new LIT("TestProject")),
                new STMT(_project, _created, new LIT(CREATED.toString(), XSD.date)),                
                new STMT(_person, RDF.type, new UID(TEST.NS, "Person")),
                new STMT(_person,  _name, new LIT("Foo Bar"))
        );             
        
        session = sessionFactory.openSession();
    }

    @After
    public void tearDown() throws IOException{
        session.close();
    }
    
    @Test
    public void Read() throws IOException {
        
        // Checking preconditions
        
        Project project = session.get(Project.class, _project);
        assertEquals("TestProject", project.name);
        
        assertEquals(0, project.infos.size());
        
        assertEquals(1, project.dates.size());
        assertTrue(project.dates.containsKey(_created));
        assertEquals(0, project.participants.size());
        session.close();
        
        // Adding dynamic data
        
        repository.add(
               new STMT(_project, _owner, _person),
               new STMT(_project, _deadline, new LIT(DEADLINE.toString(), XSD.date)),
               new STMT(_project, _description, new LIT(DESCRIPTION1)),
               new STMT(_project, _description, new LIT(DESCRIPTION2)),
               new STMT(_project, _creatorComment, new LIT(CREATOR_COMMENT))
        );

        // Checking dynamic data
        
        session = sessionFactory.openSession();
        project = session.get(Project.class, _project);
        Person person = session.get(Person.class, _person);
        assertEquals("Foo Bar", person.name);

        assertEquals(2, project.infos.size());
        
        assertEquals(2, project.dates.size());
        
        assertTrue(project.infos.containsKey(_description));
        assertTrue(project.infos.containsKey(_creatorComment));
        assertTrue(project.dates.containsKey(_created));
        assertTrue(project.dates.containsKey(_deadline));
        assertTrue(project.participants.containsKey(_owner));

        assertFalse(project.infos.containsKey(_name));
        
        assertEquals(person, project.participants.get(_owner));
        assertTrue(project.infos.get(_description).contains(DESCRIPTION1));
        assertTrue(project.infos.get(_description).contains(DESCRIPTION2));
        assertTrue(project.infos.get(_creatorComment).contains(CREATOR_COMMENT));
        assertEquals(CREATED, project.dates.get(_created));
        assertEquals(DEADLINE, project.dates.get(_deadline));
    }
    
    @Test
    public void Write(){
        Project project = new Project();
        project.dates = new HashMap<UID,LocalDate>();
        project.dates.put(_created, CREATED);
        project.dates.put(_deadline, DEADLINE);
        session.save(project);
        session.clear();
        
        Project project2 = session.getById(project.id, Project.class);
        assertEquals(project.dates, project2.dates);
    }

    
}