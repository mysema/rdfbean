/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * QueryByExampleTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class QueryByExampleTest extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS)
    public abstract class Identifiable {
        
        private static final long serialVersionUID = 4580448045434144592L;
        
        @Id(IDType.LOCAL)
        private String id;
        
        public String getId() {
            return id;
        }

        public String toString() {
            return id;
        }

    }
    
    @ClassMapping(ns=TEST.NS)
    public class User extends Identifiable {
        
        @Predicate
        private String firstName, lastName, email;
        
        @Predicate
        private String username;
        
        @Predicate
        private String password;

        @Predicate
        private Profile profile;
        
        @Predicate
        private Set<User> buddies = new HashSet<User>();
        
        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public Profile getProfile() {
            return profile;
        }

        public Set<User> getBuddies() {
            return buddies;
        }
        
        public boolean isActive(){
            return true;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public enum Profile {
        User,
        Admin        
    }
    
    @Test
    public void test() throws StoreException{
        session = createSession(User.class, Profile.class, Identifiable.class);
        User user = new User();
        user.email = "a@b.com";
        user.firstName = "Anton";
        user.lastName = "Smith";
        user.password = "pass";
        user.profile = Profile.Admin;
        session.save(user);
        
        User example = new User();
        example.email = user.getEmail();
        assertEquals(user, session.getByExample(example));
        
        example.firstName = user.getFirstName();
        assertEquals(user, session.getByExample(example));
        
        example.lastName = user.getLastName();
        assertEquals(user, session.getByExample(example));
        
        example.password = user.getPassword();
        assertEquals(user, session.getByExample(example));
        
        example.profile = user.getProfile();
        assertEquals(user, session.getByExample(example));
    }

}
