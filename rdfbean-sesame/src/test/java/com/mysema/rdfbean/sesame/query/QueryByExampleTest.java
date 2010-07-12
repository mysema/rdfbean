/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.domains.UserProfileDomain;
import com.mysema.rdfbean.domains.UserProfileDomain.Identifiable;
import com.mysema.rdfbean.domains.UserProfileDomain.Profile;
import com.mysema.rdfbean.domains.UserProfileDomain.User;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.TestConfig;

/**
 * QueryByExampleTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@TestConfig({User.class, Profile.class, Identifiable.class})
public class QueryByExampleTest extends SessionTestBase implements UserProfileDomain{
        
    @Test
    public void test() throws StoreException{
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
