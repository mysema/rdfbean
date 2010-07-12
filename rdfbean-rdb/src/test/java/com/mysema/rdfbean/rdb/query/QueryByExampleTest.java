package com.mysema.rdfbean.rdb.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.domains.UserProfileDomain;
import com.mysema.rdfbean.domains.UserProfileDomain.Identifiable;
import com.mysema.rdfbean.domains.UserProfileDomain.Profile;
import com.mysema.rdfbean.domains.UserProfileDomain.User;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.TestConfig;

@TestConfig({User.class, Profile.class, Identifiable.class})
public class QueryByExampleTest extends AbstractRDBTest implements UserProfileDomain{
    
    private Session session;
    
    @Test
    public void test(){
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
