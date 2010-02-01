package com.mysema.rdfbean.tapestry.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.dao.AbstractRepository;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.object.SimpleSessionContext;

/**
 * RepositoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RepositoryTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
        @Id(IDType.LOCAL)
        String id;
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

    private static AbstractRepository<User> repository;
    
    private static SessionFactoryImpl sessionFactory;
    
    private static SimpleSessionContext sessionContext;
    
    @BeforeClass
    public static void before() throws IOException{
        sessionFactory = new SessionFactoryImpl();
        sessionContext = new SimpleSessionContext(sessionFactory);
        sessionFactory.setSessionContext(sessionContext);
        sessionFactory.setConfiguration(new DefaultConfiguration(User.class));
        sessionFactory.setRepository(new MiniRepository());
        sessionFactory.initialize();    
        
        repository = new AbstractRepository<User>(sessionFactory, new PathBuilder<User>(User.class, "user")){};
        Session session = null;
        try{
            session = sessionFactory.openSession();
            session.save(new User("John","Smith"));
            session.save(new User("Bob","Stewart"));
            session.flush();
        }finally{
            session.close();
        }
    }
    
    @AfterClass
    public static void after(){
        sessionFactory.close();
    }
    
    @Before
    public void setUp(){
        sessionContext.getOrCreateSession();
    }
    
    @After
    public void tearDown() throws IOException{
        sessionContext.getCurrentSession().close();
        sessionContext.releaseSession();
    }
    
    @Test
    public void testGetAll() {
        assertFalse(repository.getAll().isEmpty());
    }

    @Test
    public void testGetById() {
        for (User user : repository.getAll()){
            assertNotNull(repository.getById(user.id));
        }
    }

    @Test
    public void testRemoveEntity() {
        User user = new User(String.valueOf(System.currentTimeMillis()), "Smith");
        repository.save(user);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        
        assertNotNull(repository.getById(user.id));        
        repository.remove(user);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        
        assertNull(repository.getById(user.id));
    }

    @Test
    public void testRemoveId() {
        User user = new User(String.valueOf(System.currentTimeMillis()), "Smith");
        repository.save(user);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        
        assertNotNull(repository.getById(user.id));        
        repository.remove(user.id);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        
        assertNull(repository.getById(user.id));
    }


}
