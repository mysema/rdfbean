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

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.PathBuilderFactory;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ContainerType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;

public class ConfigurationBuilderTest {
    
    public static class Person {
        
        Labeled labeled;
        
        String id;
        
        String firstName, lastName;
        
        Person superior;
        
        Department department;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
        
    }
    
    public static class Department {
        
        String id;
        
        String name;
        
        Company company;
    }
    
    public static class Company {
        
        String id;
        
        String name;
        
        Set<Department> departments;
    }
    
    public static class Labeled {
        
        String label;
        
        String comment;
    }
    
    @Test
    public void path(){
        // TODO
    }
    
    @Test
    public void mixin(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Person.class)
            .addMixin("labeled");
        Configuration configuration = builder.build();    
        
        // labeled
        MappedClass person = configuration.getMappedClass(Person.class);
        MappedPath person_labeled = person.getMappedPath("labeled");
        assertTrue(person_labeled.getMappedProperty().isMixin());
    }
    
    @Test
    public void without_namespace(){
        ConfigurationBuilder builder = new ConfigurationBuilder();
        for (Class<?> cl : Arrays.<Class<?>>asList(Person.class, Department.class, Company.class, Labeled.class)){
            builder.addClass(cl).addProperties();    
        }        
        Configuration configuration = builder.build();
        
        MappedClass person = configuration.getMappedClass(Person.class);
        String ns = "java:com.mysema.rdfbean.object.ConfigurationBuilderTest.Person#";
        assertEquals(new UID(ns,"Person"), person.getUID());
        assertEquals(new UID(ns,"labeled"), person.getMappedPath("labeled").getPredicatePath().get(0).getUID());
    }
    
    @Test
    public void container(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Company.class)
            .addId("id")
            .addProperty("departments", new UID(TEST.NS,"company"), ContainerType.SEQ)
            .addProperties();
        Configuration configuration = builder.build();
        
        // company
        MappedClass company = configuration.getMappedClass(Company.class);
        MappedPath company_departments = company.getMappedPath("departments");
        assertEquals(TEST.NS, company_departments.getPredicatePath().get(0).getUID().ns());
        assertEquals("company", company_departments.getPredicatePath().get(0).getUID().ln());
        assertEquals(ContainerType.SEQ, company_departments.getMappedProperty().getContainerType());
    }
    
    @Test
    public void localized(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Labeled.class)
            .addLocalized("label", RDFS.label)
            .addProperty("comment", RDFS.comment);
        Configuration configuration = builder.build();    
        
        // labeled
        MappedClass labeled = configuration.getMappedClass(Labeled.class);
        MappedPath labeled_label = labeled.getMappedPath("label");
        assertTrue(labeled_label.getMappedProperty().isLocalized());
        assertEquals(RDFS.label,labeled_label.getPredicatePath().get(0).getUID());
        MappedPath labeled_comment = labeled.getMappedPath("comment");
        assertFalse(labeled_comment.getMappedProperty().isLocalized());
        assertEquals(RDFS.comment,labeled_comment.getPredicatePath().get(0).getUID());
    }
    
    @Test
    public void namespaces(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Labeled.class)
            .addProperty("label", RDFS.label)
            .addProperty("comment", RDFS.comment);
        Configuration configuration = builder.build();    
        
        // labeled
        MappedClass labeled = configuration.getMappedClass(Labeled.class);
        MappedPath labeled_label = labeled.getMappedPath("label");
        assertEquals(RDFS.label,labeled_label.getPredicatePath().get(0).getUID());
        MappedPath labeled_comment = labeled.getMappedPath("comment");
        assertEquals(RDFS.comment,labeled_comment.getPredicatePath().get(0).getUID());
    }
    
    @Test
    public void inverse(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Company.class)
            .addId("id")
            .addProperty("departments", new UID(TEST.NS,"company"),true)
            .addProperties();
        Configuration configuration = builder.build();
        
        // company
        MappedClass company = configuration.getMappedClass(Company.class);
        MappedPath company_departments = company.getMappedPath("departments");
        assertEquals(TEST.NS, company_departments.getPredicatePath().get(0).getUID().ns());
        assertEquals("company", company_departments.getPredicatePath().get(0).getUID().ln());
        assertTrue(company_departments.getPredicatePath().get(0).inv());
    }
    
    @Test
    public void addProperty(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Person.class)
            .addId("id")
            .addProperty("firstName")
            .addProperty("lastName", new UID(TEST.NS, "surName"))
            .addProperty("superior")
            .addProperty("department");
        Configuration configuration = builder.build();    
        
        // person
        MappedClass person = configuration.getMappedClass(Person.class);
        MappedPath person_id = person.getMappedPath("id");
        assertTrue(person_id.getPredicatePath().isEmpty());
        MappedPath person_firstName = person.getMappedPath("firstName");
        assertEquals(TEST.NS, person_firstName.getPredicatePath().get(0).getUID().ns());
        assertEquals("firstName", person_firstName.getPredicatePath().get(0).getUID().ln());
        MappedPath person_lastName = person.getMappedPath("lastName");
        assertEquals("surName", person_lastName.getPredicatePath().get(0).getUID().ln());
        MappedPath person_superior = person.getMappedPath("superior");
        assertEquals("superior", person_superior.getPredicatePath().get(0).getUID().ln());
        MappedPath person_department = person.getMappedPath("department");
        assertEquals("department", person_department.getPredicatePath().get(0).getUID().ln());
    }
    
    @Test
    public void addProperties(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Person.class).addId("id").addProperties();
        builder.addClass(new UID(TEST.NS, "Dept"), Department.class).addId("id").addProperties();
        builder.addClass(TEST.NS, Company.class).addId("id").addProperties();
        Configuration configuration = builder.build();
        
        // person
        MappedClass person = configuration.getMappedClass(Person.class);
        MappedPath person_id = person.getMappedPath("id");
        assertTrue(person_id.getPredicatePath().isEmpty());
        MappedPath person_firstName = person.getMappedPath("firstName");
        assertEquals("firstName", person_firstName.getPredicatePath().get(0).getUID().ln());
        MappedPath person_lastName = person.getMappedPath("lastName");
        assertEquals("lastName", person_lastName.getPredicatePath().get(0).getUID().ln());
        MappedPath person_superior = person.getMappedPath("superior");
        assertEquals("superior", person_superior.getPredicatePath().get(0).getUID().ln());
        MappedPath person_department = person.getMappedPath("department");
        assertEquals("department", person_department.getPredicatePath().get(0).getUID().ln());
        
        // department
        MappedClass department = configuration.getMappedClass(Department.class);
        MappedPath department_id = department.getMappedPath("id");
        assertTrue(department_id.getPredicatePath().isEmpty());
        MappedPath department_name = department.getMappedPath("name");
        assertEquals("name", department_name.getPredicatePath().get(0).getUID().ln());
        MappedPath department_company = department.getMappedPath("company");
        assertEquals("company", department_company.getPredicatePath().get(0).getUID().ln());
        
        // company
        MappedClass company = configuration.getMappedClass(Company.class);
        MappedPath company_id = company.getMappedPath("id");
        assertTrue(company_id.getPredicatePath().isEmpty());
        MappedPath company_name = company.getMappedPath("name");
        assertEquals("name", company_name.getPredicatePath().get(0).getUID().ln());
    }

    @Test
    public void query(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Person.class).addId("id").addProperties();
        builder.addClass(new UID(TEST.NS, "Dept"), Department.class).addId("id").addProperties();
        builder.addClass(TEST.NS, Company.class).addId("id").addProperties();
        Configuration configuration = builder.build();
        
        Session session = SessionUtil.openSession(new MiniRepository(), Collections.<Locale>emptySet(), configuration);
        Person person = new Person();
        person.firstName = "Bob";
        person.lastName = "Smith";
        session.save(person);
        session.clear();
        
        // getById
        Person other = session.getById(person.id, Person.class);
        assertNotNull(other.id);
        assertEquals(person.firstName, other.firstName);
        assertEquals(person.lastName, other.lastName);
        
        // query
        PathBuilder<Person> personPath = new PathBuilderFactory().create(Person.class);
        assertEquals(other, session.from(personPath).where(personPath.getString("firstName").eq("Bob")).uniqueResult(personPath));
        assertEquals(other, session.from(personPath).where(personPath.getString("lastName").eq("Smith")).uniqueResult(personPath));
        
        // list
        assertEquals(1, session.from(personPath).list(personPath).size());
        
        // findInstances
        assertEquals(1, session.findInstances(Person.class).size());
        
        
    }
    
}
