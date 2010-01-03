/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;

/**
 * RDFBeanGridDataSourceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDFBeanGridDataSourceTest {
    
    private static SessionFactoryImpl sessionFactory;
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
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
    
    @BeforeClass
    public static void before() throws IOException{
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration(User.class));
        sessionFactory.setRepository(new MiniRepository());
        sessionFactory.initialize();
        
        Session session = sessionFactory.openSession();
        try{
            for (char c = 'A'; c < 'Z'; c++){
                for (int i = 0; i < 10; i++){
                    session.save(new User(String.valueOf(c)+i, String.valueOf(c+i)+i));
                }    
            }    
        }finally{
            session.close();    
        }
    }
    
    @AfterClass
    public static void after() throws IOException{
        sessionFactory.close();
    }
    
    private GridDataSource dataSource;
    
    @Before
    public void setUp(){
        dataSource = new RDFBeanGridDataSource<User>(sessionFactory, User.class);
    }

    @Test
    public void testGetAvailableRows() {
        assertEquals(250, dataSource.getAvailableRows());
    }

    @Test
    public void testPrepare() {
        dataSource.prepare(0, 10, Collections.<SortConstraint>emptyList());
    }

    @Test
    public void testGetRowValue() {
        // TODO
    }

    @Test
    public void testGetRowType() {
        assertEquals(User.class, dataSource.getRowType());
    }

}
