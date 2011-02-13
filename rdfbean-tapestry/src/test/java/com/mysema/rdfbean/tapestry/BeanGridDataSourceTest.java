/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.grid.ColumnSort;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.sesame.MemoryRepository;

public class BeanGridDataSourceTest {
    
    // TODO : better tests
    
    private static SessionFactoryImpl sessionFactory;
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
	@Id
	String id;
	
        @Predicate
        private String firstName;
        
        @Predicate
        private String lastName;
        
        public User(){}
        
        public User(String firstName, String lastName){
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
        
        
    }
    
    @BeforeClass
    public static void before() throws IOException{
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration(User.class));
        sessionFactory.setRepository(new MemoryRepository());
        sessionFactory.initialize();
        
        Session session = sessionFactory.openSession();
        try{
            for (char c = 'A'; c < 'Z'; c++){
                for (int i = 0; i < 10; i++){
                    String firstName = String.valueOf(c)+i;
                    String lastName = String.valueOf(c+i)+i;
                    if (i % 2 == 0){
                        firstName = firstName.toLowerCase();
                    }
                    session.save(new User(firstName, lastName));
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
        User user = Alias.alias(User.class);        
        dataSource = new BeanGridDataSource<User>(sessionFactory, $(user), $(user.getFirstName()).asc(), true);
    }

    @Test
    public void GetAvailableRows() {
        assertEquals(250, dataSource.getAvailableRows());
    }

    @Test
    public void Prepare() {
        dataSource.prepare(0, 10, Collections.<SortConstraint>emptyList());
    }
    
    @Test
    public void Prepare_with_sort() {
	PropertyModel firstName = new SimplePropertyModel("firstName",String.class);
	SortConstraint constraint = new SortConstraint(firstName,ColumnSort.ASCENDING);
	dataSource.prepare(0, 10, Collections.singletonList(constraint));
    }

    @Test
    public void GetRowValue() {
        dataSource.prepare(0, 9, Collections.<SortConstraint>emptyList());
        for (int i = 0; i < 10; i++){
            dataSource.getRowValue(i);
        }
        
        dataSource.prepare(10, 19, Collections.<SortConstraint>emptyList());
        for (int i = 10; i < 20; i++){
            dataSource.getRowValue(i);
        }
        
        // ...
        
        dataSource.prepare(240, 249, Collections.<SortConstraint>emptyList());
        for (int i = 240; i < 250; i++){
            dataSource.getRowValue(i);
        }
    }
    
    // TODO : proper test for case sensitivity
    
    @Test
    public void GetRowType() {
        assertEquals(User.class, dataSource.getRowType());
    }

}
