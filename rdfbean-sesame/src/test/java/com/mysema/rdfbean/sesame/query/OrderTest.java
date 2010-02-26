/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.sesame.SessionTestBase;


/**
 * OrderTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OrderTest extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
        
        @Predicate
        private String firstName;
        
        public User(){}
        
        public User(String firstName){
            this.firstName = firstName;            
        }
        
        public String getFirstName(){
            return firstName;
        }
                
    }
    
    @Test
    public void testOrderBy() throws StoreException, IOException{
        session = createSession(User.class);
        
        session.save(new User());
        User user = Alias.alias(User.class, "user");
        assertFalse(session.from($(user))
                .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());
        
        assertFalse(session.from($(user))
                .where($(user.getFirstName()).isNull())
                .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());
    }
    
    @Test
    public void correctOrder() throws StoreException, IOException{
        session = createSession(User.class);     
        for(User user : session.findInstances(User.class)){
            session.delete(user);
        }
        
        for (String name : Arrays.asList("C","A","D","B")){
            session.save(new User(name));    
        }
        
        User user = Alias.alias(User.class, "user");
        List<String> results = session.from($(user))
            .orderBy($(user.getFirstName()).asc())
            .list($(user.getFirstName()));
        assertEquals(Arrays.asList("A","B","C","D"), results);
    }
    
    @Test
    public void orderWithOffset() throws StoreException, IOException{
        session = createSession(User.class);     
        for(User user : session.findInstances(User.class)){
            session.delete(user);
        }
        
        for (String name : Arrays.asList("C","A","D","B")){
            session.save(new User(name));    
        }
        
        // #1
        User user = Alias.alias(User.class, "user");
        List<String> results = session.from($(user))
            .orderBy($(user.getFirstName()).asc())
            .offset(1)
            .list($(user.getFirstName()));
        assertEquals(Arrays.asList("B","C","D"), results);

        // #2
        results = session.from($(user))
            .orderBy($(user.getFirstName()).asc())
            .limit(3)
            .list($(user.getFirstName()));
        assertEquals(Arrays.asList("A","B","C"), results);
        
        // #3
        results = session.from($(user))
            .orderBy($(user.getFirstName()).asc())
            .offset(1)
            .limit(2)
            .list($(user.getFirstName()));
        assertEquals(Arrays.asList("B","C"), results);
    }

}
