package com.mysema.rdfbean.tapestry.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;

/**
 * SeedEntityTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SeedEntityTest {
    
    private static SessionFactoryImpl sessionFactory;
    
    private Session session;
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
        @Id(IDType.RESOURCE)
        ID id;
        @Predicate
        String firstName;
        @Predicate
        String lastName;
        
        public User(){}
        
        public User(String firstName, String lastName){
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Article{
        @Id(IDType.RESOURCE)
        ID id;
        @Predicate
        User author;
    }
    
    @BeforeClass
    public static void before() throws IOException{
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration(User.class, Article.class));
        sessionFactory.setRepository(new MiniRepository());
        sessionFactory.initialize();        
    }
    
    @AfterClass
    public static void after(){
        sessionFactory.close();
    }
    
    @After
    public void tearDown() throws IOException{
        if (session != null) session.close();
    }
    
    @Test
    @Ignore
    public void test() throws IOException{
        List<Object> entities = Arrays.<Object>asList(new User("John","Smith"), new User("Bob","Stewart"));
        new SeedEntityImpl(sessionFactory, entities);
        
        session = sessionFactory.openSession();
        assertEquals(2, session.findInstances(User.class).size());
        
        new SeedEntityImpl(sessionFactory, entities);
        assertEquals(2, session.findInstances(User.class).size());
        
    }

}
