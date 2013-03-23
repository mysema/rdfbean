/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface CompanyDepartmentEmployeeDomain {

    @ClassMapping
    public class Company {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate(ln = "company", inv = true)
        public List<Department> departments = new ArrayList<Department>();

        @Predicate(ln = "company", inv = true)
        public Department department;

        public ID getId() {
            return id;
        }

        public List<Department> getDepartments() {
            return departments;
        }

        public Department getDepartment() {
            return department;
        }

    }

    @ClassMapping
    public class Department {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public Company company;

        @Predicate(ln = "department", inv = true)
        public Set<Employee> employees = new HashSet<Employee>();

        public ID getId() {
            return id;
        }

        public Company getCompany() {
            return company;
        }

        public Set<Employee> getEmployees() {
            return employees;
        }

    }

    @ClassMapping
    public class Employee {

        @Id(IDType.RESOURCE)
        public ID id;

        @Predicate
        public Department department;

        public ID getId() {
            return id;
        }

        public Department getDepartment() {
            return department;
        }

    }

}
