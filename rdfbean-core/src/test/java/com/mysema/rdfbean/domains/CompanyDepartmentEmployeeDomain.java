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
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Department {
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public Company company;
        
        @Predicate(ln="department", inv=true)
        public Set<Employee> employees;
    }
    
    @ClassMapping(ns=TEST.NS)
    public class Employee {
        
        @Id(IDType.RESOURCE)
        public ID id;
        
        @Predicate
        public Department department;
        
    }
    
}
