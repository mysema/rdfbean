/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.store;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * LuceneViaSessionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SessionPersistenceTest extends AbstractStoreTest{

    @ClassMapping(ns=TEST.NS)
    public static class Employee{
        @Id(IDType.RESOURCE)
        ID id;        
        
        @Predicate
        String firstName, lastName;   
        
        @Predicate
        Department department;
        
        @Predicate
        Employee superior;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Department{
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate
        Company company;
        
        @Predicate(ns=RDFS.NS, ln="label")
        String name;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Company{
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate(ns=RDFS.NS, ln="label")
        String name;
    }
    
    @Test
    public void test() throws IOException{
        Session session = SessionUtil.openSession(repository, 
                Employee.class, Department.class, Company.class);
        Company company = new Company();
        company.name = "Big Company";
        
        Department department = new Department();
        department.name = "Marketing";
        department.company = company;
        
        Employee employee = new Employee();
        employee.department = department;
        employee.firstName = "John";
        employee.lastName = "Smith";
        
        session.save(company);
        session.save(department);
        session.save(employee);        
        assertNotNull(company.id);
        assertNotNull(department.id);
        assertNotNull(employee);
        
        session.flush();
        session.clear();
        
        assertNotNull(session.get(Company.class, company.id));
        assertNotNull(session.get(Department.class, department.id));
        assertNotNull(session.get(Employee.class, employee.id));
        
        session.close();
    }

    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration(Employee.class, Department.class, Company.class);
    }
}
