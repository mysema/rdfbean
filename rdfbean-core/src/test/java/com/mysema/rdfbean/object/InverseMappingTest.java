/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;

public class InverseMappingTest {

    @ClassMapping
    public static class Company {

        @Id
        ID id;

        @Predicate(ln = "company", inv = true)
        Set<Department> departments;
    }

    @ClassMapping
    public static class Department {

        @Id
        ID id;

        @Predicate
        Company company;

        @Predicate(ln = "department", inv = true)
        Set<Employee> employees;
    }

    @ClassMapping
    public static class Employee {

        @Id
        ID id;

        @Predicate
        Department department;

    }

    @Test
    public void test() {
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
