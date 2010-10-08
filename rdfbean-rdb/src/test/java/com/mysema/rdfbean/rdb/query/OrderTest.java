/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.UserDomain;
import com.mysema.rdfbean.domains.UserDomain.User;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig(User.class)
public class OrderTest extends AbstractRDBTest implements UserDomain{
    
    private User user = Alias.alias(User.class, "u");
    
    @Test
    public void OrderBy() throws  IOException{
        session.save(new User());
        assertFalse(session.from($(user))
                .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());
        
        assertFalse(session.from($(user))
                .where($(user.getFirstName()).isNull())
                .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());
    }
    
    @Test
    public void CorrectOrder() throws  IOException{
        for (User user : session.findInstances(User.class)){
            session.delete(user);
        }
        
        for (String name : Arrays.asList("C","A","D","B")){
            session.save(new User(name));    
        }
        
        List<String> results = session.from($(user))
            .orderBy($(user.getFirstName()).asc())
            .list($(user.getFirstName()));
        assertEquals(Arrays.asList("A","B","C","D"), results);
    }
    
    @Test
    public void OrderWithOffset() throws  IOException{  
        for(User user : session.findInstances(User.class)){
            session.delete(user);
        }
        
        for (String name : Arrays.asList("C","A","D","B")){
            session.save(new User(name));    
        }
        
        List<String> results;
        
        // #1
//        List<String> results = session.from($(user))
//            .orderBy($(user.getFirstName()).asc())
//            .offset(1)
//            .list($(user.getFirstName()));
//        assertEquals(Arrays.asList("B","C","D"), results);

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
