/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static com.mysema.query.alias.Alias.$;
import static com.mysema.query.alias.Alias.alias;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.l10n.support.LocaleIterable;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Localized;
import com.mysema.rdfbean.annotations.Path;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.MiniRepository;

public class UpdateTest {

    @ClassMapping(ns=TEST.NS)
    public static class Employee {
        
        @Id(IDType.RESOURCE)
        ID id;
            
        @Predicate
        String name;
        
        @Predicate
        int age;
        
        @Predicate
        Company company;

        public Employee() {}
        
        public Employee(String name, int age, Company company) {
            this.name = name;
            this.age = age;
            this.company = company;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public Company getCompany() {
            return company;
        }
        
        
    
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Company {
       
        @Id(IDType.RESOURCE)
        ID id;
       
        @Predicate(ln="company", inv=true)
        Set<Employee> employees = new LinkedHashSet<Employee>();
    
        @Predicate
        List<Employee> managers = new ArrayList<Employee>();
        
        @Predicate
        @Localized
        @Nullable
        String description;
        
        @Predicate
        @Nullable
        String name;

        public Company() {}
        
        public Company(String name) {
            this.name = name;
        }
        
        // Getter provided for Querydsl MiniApi
        public String getName() {
            return name; 
        }
        
        public String getDescription(){
            return description;
        }
    }
    
    @ClassMapping(ns=TEST.NS, ln="Employee")
    public static class EmployeeInfo {
        
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate
        String name;
        
        @Path({@Predicate(ln="company"), @Predicate(ln="name")})
        String companyName;
    
    }

    private List<LID> ids;
    
    private MiniRepository repository;
    
    private Session session;
    
    private SessionFactoryImpl sessionFactory;

    private Locale locale;
    
    @Before
    public void init() {
        Company company = new Company();
        company.name = "Example";
        company.description = "In English";
        
        Employee employee = new Employee();
        employee.age = 30;
        employee.name = "John Doe";
        employee.company = company;
        
        repository = new MiniRepository();
        sessionFactory = new SessionFactoryImpl() {

            @Override
            public Iterable<Locale> getLocales() {
                return new LocaleIterable(locale, false);
            }
            
        };
        sessionFactory.setRepository(repository);
        DefaultConfiguration configuration = new DefaultConfiguration(Employee.class, Company.class, EmployeeInfo.class);
//        configuration.setFetchStrategies(Collections.<FetchStrategy>emptyList());
        sessionFactory.setConfiguration(configuration);
        sessionFactory.initialize();
        ids = newSession().saveAll(employee, company);
    }

    private Session newSession() {
        return newSession(Locale.ENGLISH);
    }

    private Session newSession(Locale locale) {
        this.locale = locale;
        session = sessionFactory.openSession();
        return session;
    }
    
    private Employee getEmployee() {
        return session.get(Employee.class, ids.get(0));
    }
    
    private Company getCompany() {
        return session.get(Company.class, ids.get(1));
    }
    
    @Test
    public void InitialData() {
        newSession();
        Employee employee = getEmployee();
        Company company = getCompany();
        
        assertEquals("John Doe", employee.name);
        assertEquals(1, company.employees.size());
        assertTrue(company.employees.contains(employee));
    }
    
    @Test
    public void UpdateEmployeeName() {
        newSession();
        Employee employee = getEmployee();
        
        employee.name = "Jane Doe";
        session.save(employee);
        
        newSession();
        employee = getEmployee();
        assertEquals("Jane Doe", employee.name);
    }
    
    @Test
    public void CompanyNameCascaded() {
        newSession();
        Employee employee = getEmployee();
        Company company = getCompany();

        company.name = "Bad example";
        session.save(employee);
        
        newSession();
        company = getCompany();
        assertEquals("Bad example", company.name);
    }
    
    @Test
    @Ignore
    public void ChangeCompany() {
        newSession();
        Employee employee = getEmployee();

        Company company = new Company();
        company.name = "Competitor";
        employee.company = company;
        session.save(employee);
        
        newSession();
        Company qCompany = alias(Company.class, "company");
        List<Company> companies = session
            .from($(qCompany))
            .orderBy($(qCompany.getName()).asc()) // Competitor, Example
            .list($(qCompany));
        
        assertEquals(2, companies.size());
        assertEquals("Competitor", companies.get(0).name);
        assertEquals("Example", companies.get(1).name);
    }
    
    @Test
    public void UpdateManagers() {
        newSession();
        Company company = getCompany();
        
        // Add new Big Boss and John Doe as managers
        Employee boss = new Employee("Big Boss", 55, company);
        company.managers.add(boss);
        company.managers.add(getEmployee());
        session.save(company);
        
        newSession();
        company = getCompany();
        
        assertEquals(2, company.managers.size());
        assertEquals("Big Boss", company.managers.get(0).name);
        
        int rsize = IteratorAdapter.asList(repository.findStatements(null, null, null, null, false)).size();
        
        // Promote John Doe in manager list
        boss = company.managers.get(0);
        boss.name = "Ex-Boss";
        company.managers.set(0, company.managers.get(1));
        company.managers.set(1, boss);
        session.save(company);

        newSession();
        company = getCompany();
        assertEquals(2, company.managers.size());
        assertEquals("John Doe", company.managers.get(0).name);
        assertEquals("Ex-Boss", company.managers.get(1).name);
        
        // See that there's no garbage left...
        assertEquals(rsize, IteratorAdapter.asList(repository.findStatements(null, null, null, null, false)).size());
    }
    
    @Test
    public void UpdateProjection() {
        EmployeeInfo einfo = session.get(EmployeeInfo.class, ids.get(0));
        assertEquals("John Doe", einfo.name);
        assertEquals("Example", einfo.companyName);
        
        einfo.name = "Jane Doe";
        einfo.companyName = "Ignore this";
        session.save(einfo);
        
        newSession();
        Employee employee = getEmployee();
        assertEquals("Jane Doe", employee.name);
        // Nothing else should have changed
        assertEquals(30, employee.age);
        assertNotNull(employee.company);
        assertEquals("Example", employee.company.name);
    }
    
    @Test
    public void LocalizedProperty() {
        newSession(Locale.UK);
        Company company = getCompany();
        assertEquals("In English", company.description);

        company.description = "In UK English";
        session.save(company);
        
        newSession(Locale.UK);
        company = getCompany();
        assertEquals("In UK English", company.description);
        
        newSession(Locale.ENGLISH);
        company = getCompany();
        assertEquals("In English", company.description);
    }
    
    @Test
    public void GetCompanyByExample(){
        newSession();
        Company company = getCompany();
        
        Company example = new Company();
        example.description = company.description;
        assertEquals(company, session.getByExample(example));
        
        example.name = company.name;
        assertEquals(company, session.getByExample(example));
        
        example.description = "XXX";
        assertNull(session.getByExample(example));
        
        example.name = null;
        example.description = null;
        assertNull(session.getByExample(example));               
    }
    
    @Test
    public void GetEmployeeByExample(){
        newSession();
        Employee employee = getEmployee();
        
        Employee example = new Employee();
        example.age = employee.age;
        assertEquals(employee, session.getByExample(example));
        
        example.company = employee.company;
        assertEquals(employee, session.getByExample(example));
        
        example.name = employee.name;
        assertEquals(employee, session.getByExample(example));
        
        example.name = "XXX";
        assertNull(session.getByExample(example));
    }
}
