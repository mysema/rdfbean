/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
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
 * PagedBeanQueryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PagedBeanQueryTest {
    
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

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
        
        
    }
    
    private SessionFactoryImpl sessionFactory;
    
    @Before
    public void setUp() throws IOException{
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration(User.class));
        sessionFactory.setRepository(new MiniRepository());
        sessionFactory.initialize();
        
        Session session = sessionFactory.openSession();
        session.save(new User("Anton", "Bruxner"));
        session.save(new User("John", "Smith"));
        session.save(new User("Zoe", "Ark"));
        session.save(new User("Chris", "Rock"));
        session.save(new User("Bernard", "Shaw"));
        session.close();
    }

    @Test
    public void orderByFirstName(){
        BeanListSourceBuilder query = new BeanListSourceBuilder(sessionFactory);
        User user = Alias.alias(User.class);
        assertEquals(
            Arrays.asList("Anton", "Bernard", "Chris", "John", "Zoe"),
            query.from($(user)).orderBy($(user.getFirstName()).asc()).list($(user.getFirstName())).getResults(0, 5));
        
        assertEquals(
                Arrays.asList("Bernard", "Chris", "John", "Zoe"),
                query.from($(user)).orderBy($(user.getFirstName()).asc()).list($(user.getFirstName())).getResults(1, 5));
    }
    
    @Test
    public void orderByLastName(){
        BeanListSourceBuilder query = new BeanListSourceBuilder(sessionFactory);
        User user = Alias.alias(User.class);
        assertEquals(
            Arrays.asList("Ark", "Bruxner", "Rock", "Shaw", "Smith"),
            query.from($(user)).orderBy($(user.getLastName()).asc()).list($(user.getLastName())).getResults(0, 5));
        
        assertEquals(
                Arrays.asList("Bruxner", "Rock", "Shaw", "Smith"),
                query.from($(user)).orderBy($(user.getLastName()).asc()).list($(user.getLastName())).getResults(1, 5));
    }
}
