package com.mysema.rdfbean.rdb;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.query.collections.MiniApi;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

public class RDBQueryTest extends AbstractRDBTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class User {
        
        @Id
        private String id;
     
        @Predicate
        private Department department;
        
        @Predicate
        private String userName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Department getDepartment() {
            return department;
        }

        public void setDepartment(Department department) {
            this.department = department;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }      
                
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Department {
        
        @Id
        private String id;
        
        @Predicate
        private Company company;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Company getCompany() {
            return company;
        }

        public void setCompany(Company company) {
            this.company = company;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Company {
        
        @Id
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
        
    }
    
    private Session session;
    
    private User[] users = new User[10];
    
    private User u = Alias.alias(User.class);
    
    @Before
    public void setUp(){
        session = SessionUtil.openSession(repository, User.class, Department.class, Company.class);
        session.setFlushMode(FlushMode.ALWAYS);
        
        for (int i = 0; i < users.length; i++){
            users[i] = new User();
            users[i].setUserName(UUID.randomUUID().toString());
            session.save(users[i]);
        }
    }
    
    @After
    public void tearDown() throws IOException{
        if (session != null){
            session.close();
        }
    }
    
    private BeanQuery query(){
        return session.createQuery(QueryLanguage.QUERYDSL);
    }
    
    @Test
    public void fromUser_list(){
        List<String> names = MiniApi.from(u, Arrays.asList(users)).list($(u.getUserName()));
        List<String> queriedNames = query().from($(u)).list($(u.getUserName()));
        assertTrue(queriedNames.containsAll(names));
    }
    
    @Test
    public void fromUser_count(){
        long count = query().from($(u)).count();
        session.save(new User());
        assertEquals(count + 1l, query().from($(u)).count());
    }
    
    @Test
    public void fromUser_where_userName_eq_constant(){        
        for (int i = 0; i < users.length; i++){
            assertEquals(users[i].getUserName(), query().from($(u))
                .where($(u.getUserName()).eq(users[i].getUserName()))
                .uniqueResult($(u.getUserName())));    
        }        
    }

    @Test
    public void fromUser_where_userName_startsWith_constant(){
        for (int i = 0; i < users.length; i++){
            assertEquals(users[i].getUserName(), query().from($(u))
                .where($(u.getUserName()).startsWith(users[i].getUserName().substring(0,users[i].getUserName().length()-1)))
                .uniqueResult($(u.getUserName())));    
        }        
    }
    
    @Test
    public void fromUser_where_userName_endsWith_constant(){
        for (int i = 0; i < users.length; i++){
            assertEquals(users[i].getUserName(), query().from($(u))
                .where($(u.getUserName()).endsWith(users[i].getUserName().substring(1)))
                .uniqueResult($(u.getUserName())));    
        }        
    }
    
}
