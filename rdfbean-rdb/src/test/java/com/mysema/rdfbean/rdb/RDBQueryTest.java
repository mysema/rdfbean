/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.query.collections.MiniApi;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Company;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Department;
import com.mysema.rdfbean.domains.UserDomain.User;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({User.class, Department.class, Company.class})
public class RDBQueryTest extends AbstractRDBTest implements UserDepartmentCompanyDomain{
    
    private User[] users = new User[10];
    
    private User u = Alias.alias(User.class);
    
    @Before
    public void setUp(){
        for (int i = 0; i < users.length; i++){
            users[i] = new User();
            users[i].userName = UUID.randomUUID().toString();
            session.save(users[i]);
        }
    }
    
    @Test
    public void FromUser_list(){
        List<String> names = MiniApi.from(u, Arrays.asList(users)).list($(u.getUserName()));
        List<String> queriedNames = session.from($(u)).list($(u.getUserName()));
        assertTrue(queriedNames.containsAll(names));
    }
    
    @Test
    public void FromUser_count(){
        long count = session.from($(u)).count();
        session.save(new User());
        assertEquals(count + 1l, session.from($(u)).count());
    }
    
    @Test
    public void FromUser_where_userName_eq_constant(){        
        for (int i = 0; i < users.length; i++){
            assertEquals(users[i].getUserName(), session.from($(u))
                .where($(u.getUserName()).eq(users[i].getUserName()))
                .uniqueResult($(u.getUserName())));    
        }        
    }

    @Test
    public void FromUser_where_userName_startsWith_constant(){
        for (int i = 0; i < users.length; i++){
            assertEquals(users[i].getUserName(), session.from($(u))
                .where($(u.getUserName()).startsWith(users[i].getUserName().substring(0,users[i].getUserName().length()-1)))
                .uniqueResult($(u.getUserName())));    
        }        
    }
    
    @Test
    public void FromUser_where_userName_endsWith_constant(){
        for (int i = 0; i < users.length; i++){
            assertEquals(users[i].getUserName(), session.from($(u))
                .where($(u.getUserName()).endsWith(users[i].getUserName().substring(1)))
                .uniqueResult($(u.getUserName())));    
        }        
    }
    
}
