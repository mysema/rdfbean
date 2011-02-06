/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.query.types.EntityPath;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Company;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Department;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.User;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({User.class, Department.class, Company.class})
public class JoinsTest extends AbstractRDBTest implements UserDepartmentCompanyDomain{
    
    private User u = Alias.alias(User.class);
    
    private User sample;
    
    @Before
    public void setUp(){
        sample = new User();
        sample.userName = "Bobby";
        session.save(sample);
    }
    
    @Test
    public void ResultSetAssertions(){
        assertEquals(1l, from($(u)).count());
        assertEquals(1l, from($(u)).list($(u.getUserName())).size());
        
        // where 1
        assertEquals(1l, from($(u)).where($(u).eq(sample)).count());
        assertEquals(0l, from($(u)).where($(u).ne(sample)).count());
        assertEquals(1, from($(u)).where($(u).eq(sample)).list($(u)).size());
        assertEquals(0, from($(u)).where($(u).ne(sample)).list($(u)).size());
        
        // where 2
        assertEquals(1l, from($(u)).where($(u.getUserName()).eq("Bobby")).count());
        assertEquals(0l, from($(u)).where($(u.getUserName()).ne("Bobby")).count());
        assertEquals(1, from($(u)).where($(u.getUserName()).eq("Bobby")).list($(u)).size());
        assertEquals(0, from($(u)).where($(u.getUserName()).ne("Bobby")).list($(u)).size());
    }
    
//    @Test
//    public void From_list(){
//        assertEquals(2, countJoins(from($(u)).createQuery($(u))));
//        // u rdf:type  :User
//        // u-symbol
//    }    
//    
//    @Test
//    public void From_where_list(){                
//        assertEquals(2, countJoins(from($(u)).where($(u).eq(sample)).createQuery($(u))));
//        // u rdf:type  :User
//        // u-symbol
//    }
//    
//    @Test
//    public void From_where2_list(){                
//        assertEquals(3, countJoins(from($(u)).where($(u.getUserName()).eq("Bobby")).createQuery($(u))));
//        // u rdf:type  :User
//        // u :userName  username
//        // u-symbol
//    }    
//    
//    @Test
//    public void From_listName(){
//        assertEquals(3, countJoins(from($(u)).createQuery($(u.getUserName()))));
//        // u rdf:type  :User
//        // u :userName userName
//        // userName-symbol        
//    }
//    
//    @Test
//    public void From_where_listName(){
//        assertEquals(3, countJoins(from($(u)).where($(u).eq(sample)).createQuery($(u.getUserName()))));
//        // u rdf:type  :User
//        // u :userName userName
//        // userName-symbol        
//    }
//    
//    @Test
//    public void From_where2_listName(){
//        assertEquals(3, countJoins(from($(u)).where($(u.getUserName()).eq("Bobby")).createQuery($(u.getUserName()))));
//        // u rdf:type  :User
//        // u :userName userName
//        // userName-symbol        
//    }    
    
    private BeanQuery from(EntityPath<?> entity){
        return session.from(entity);
    }
    
//    private int countJoins(SQLQuery query){
//        return ((AbstractSQLQuery<?>)query).getMetadata().getJoins().size();
//    }
}
