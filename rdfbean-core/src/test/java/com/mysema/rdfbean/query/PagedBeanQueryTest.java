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
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;

@Ignore
public class PagedBeanQueryTest {

    private SessionFactoryImpl sessionFactory;

    @Before
    public void setUp() throws IOException {
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
    public void OrderByFirstName() {
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
    public void OrderByLastName() {
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
