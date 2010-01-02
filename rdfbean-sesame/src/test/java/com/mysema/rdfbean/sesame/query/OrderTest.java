/*
 * Copyright (c) 2009 Mysema Ltd.
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
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.SessionTestBase;


/**
 * OrderTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OrderTest extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS, ln="OrderTest_User")
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
        Session session = createSession(User.class);
        
        try{
            session.save(new User());
            User user = Alias.alias(User.class, "user");
            assertFalse(session.from($(user))
                    .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());
            
            assertFalse(session.from($(user))
                    .where($(user.getFirstName()).isNull())
                    .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());    
        }finally{
            session.close();
        }        
    }
    
    @Test
    public void correctOrder() throws StoreException, IOException{
        Session session = createSession(User.class);        
        session.save(new User("C"));
        session.save(new User("A"));
        session.save(new User("D"));
        session.save(new User("B"));
        
        User user = Alias.alias(User.class, "user");
        List<String> results = session.from($(user))
            .where($(user.getFirstName()).isNotNull())
            .orderBy($(user.getFirstName()).asc())
            .list($(user.getFirstName()));
        session.close();
        assertEquals(Arrays.asList("A","B","C","D"), results);
    }

}
