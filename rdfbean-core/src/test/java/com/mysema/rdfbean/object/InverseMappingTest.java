package com.mysema.rdfbean.object;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public class InverseMappingTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class Company {
        
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate(ln="company", inv=true)
        List<Department> departments;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Department {
        
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate
        Company company;
        
        @Predicate(ln="department", inv=true)
        Set<Employee> employees;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Employee {
        
        @Id(IDType.RESOURCE)
        ID id;
        
        @Predicate
        Department department;
        
    }
    
    @Test
    public void test(){
        Session session = SessionUtil.openSession(Company.class, Department.class, Employee.class);
        Company company = new Company();
        Department department = new Department();
        Employee employee = new Employee();
        employee.department = department;
        department.company = company;
        
        session.save(company);
        session.save(department);
        session.save(employee);
        session.flush();
        session.clear();
        
        department = session.get(Department.class, department.id);
        assertFalse(department.employees.isEmpty());
        assertTrue(department.employees.iterator().next() instanceof Employee);
                
        company = session.get(Company.class, company.id);
        assertFalse(company.departments.isEmpty());
        assertTrue(company.departments.iterator().next() instanceof Department);
    }

}
