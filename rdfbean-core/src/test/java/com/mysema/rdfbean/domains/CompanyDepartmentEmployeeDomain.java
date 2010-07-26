/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import java.util.List;
import java.util.Set;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

public interface CompanyDepartmentEmployeeDomain {

    @ClassMapping(ns=TEST.NS)
    public class Company {
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate(ln="company", inv=true)
        public List<Department> departments;

        @Predicate(ln="company", inv=true)
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
    
    @ClassMapping(ns=TEST.NS)
    public class Department {
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public Company company;
        
        @Predicate(ln="department", inv=true)
        public Set<Employee> employees;

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
    
    @ClassMapping(ns=TEST.NS)
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
