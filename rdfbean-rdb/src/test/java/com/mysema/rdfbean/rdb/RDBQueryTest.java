package com.mysema.rdfbean.rdb;

import static org.junit.Assert.*;

import java.util.Locale;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.PathBuilderFactory;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

public class RDBQueryTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class User {
        
        @Id
        String id;
     
        @Predicate
        Department department;
        
        @Predicate
        String userName;      
                
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Department {
        
        @Id
        String id;
        
        @Predicate
        Company company;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Company {
        
        @Id
        String id;
        
    }
    
    private static PathBuilderFactory pathBuilderFactory = new PathBuilderFactory();
    
    private static RDBContext context;
    
    private static Session session;
    
    private RDBQuery query;
    
    private PathBuilder<User> user = pathBuilderFactory.create(User.class);    
    private PathBuilder<Department> department = pathBuilderFactory.create(Department.class);    
    private PathBuilder<Company> company = pathBuilderFactory.create(Company.class);
    
    @BeforeClass
    public static void setUpClass(){
        context = new RDBContext(
                new MD5IdFactory(),
                new DualHashBidiMap<NODE,Long>(),
                new DualHashBidiMap<Locale,Integer>(),
                new MemoryIdSequence(),
                null,
                new H2Templates().newLineToSingleSpace());    
        session = SessionUtil.openSession(User.class, Department.class, Company.class);
    }
    
    @Before
    public void setUp(){
        query = new RDBQuery(context, session);
    }
    
    @Test
    public void fromUser(){
        query.from(user);
        assertEquals("from STATEMENT user " +
        	     "where user.PREDICATE = ? and user.OBJECT = ?", query.toString());
    }
    
    @Test
    public void fromUser_and_Department(){
        query.from(user, department);
        assertEquals("from STATEMENT user, STATEMENT department " +
        	     "where user.PREDICATE = ? and user.OBJECT = ? and " +
        	     "department.PREDICATE = ? and department.OBJECT = ?", query.toString());
        
    }
    
    @Test
    public void fromUser_and_Department_is_not_null(){
        query.from(user).where(user.get("department", Department.class).isNotNull());
        assertEquals("from STATEMENT user inner join STATEMENT user_department on user.SUBJECT = user_department.SUBJECT " +
        	     "where user.PREDICATE = ? and user.OBJECT = ? and " +
        	     "user_department.PREDICATE = ? and " +
        	     "user_department is not null", query.toString());
    }
    
    @Test
    public void fromUser_and_Department_is_null(){
        query.from(user).where(user.get("department", Department.class).isNull());
        assertEquals("from STATEMENT user inner join STATEMENT user_department on user.SUBJECT = user_department.SUBJECT " +
        	     "where user.PREDICATE = ? and user.OBJECT = ? and " +
        	     "user_department.PREDICATE = ? and user_department is null", query.toString());
    }

}
