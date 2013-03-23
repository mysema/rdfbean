package com.mysema.rdfbean.tapestry.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class SeedEntityTest {

    private static Configuration configuration;

    private static SessionFactoryImpl sessionFactory;

    private Session session;

    @ClassMapping(ns = TEST.NS)
    public static class User {
        @Id(IDType.RESOURCE)
        ID id;
        @Predicate
        String firstName;
        @Predicate
        String lastName;

        public User() {
        }

        public User(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    @ClassMapping(ns = TEST.NS)
    public static class Article {
        @Id(IDType.RESOURCE)
        ID id;
        @Predicate
        User author;
    }

    @BeforeClass
    public static void before() throws IOException {
        configuration = new DefaultConfiguration(User.class, Article.class);
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(new MemoryRepository());
        sessionFactory.initialize();
    }

    @AfterClass
    public static void after() {
        sessionFactory.close();
    }

    @After
    public void tearDown() throws IOException {
        if (session != null)
            session.close();
    }

    @Test
    public void test() throws IOException {
        List<Object> entities = Arrays.<Object> asList(new User("John", "Smith"), new User("Bob", "Stewart"));
        new SeedEntityImpl(configuration, sessionFactory, entities);

        session = sessionFactory.openSession();
        assertEquals(2, session.findInstances(User.class).size());

        new SeedEntityImpl(configuration, sessionFactory, entities);
        assertEquals(2, session.findInstances(User.class).size());

    }

}
