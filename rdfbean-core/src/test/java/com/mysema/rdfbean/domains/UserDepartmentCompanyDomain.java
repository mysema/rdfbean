/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;

public interface UserDepartmentCompanyDomain {

    @ClassMapping(ns=TEST.NS)
    public static class User {
        
        @Id
        public String id;
     
        @Predicate
        public Department department;
        
        @Predicate
        public String userName;

        public String getId() {
            return id;
        }

        public Department getDepartment() {
            return department;
        }

        public String getUserName() {
            return userName;
        }

    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Department {
        
        @Id
        public String id;
        
        @Predicate
        public Company company;

        public String getId() {
            return id;
        }

        public Company getCompany() {
            return company;
        }
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Company {
        
        @Id
        public String id;

        public String getId() {
            return id;
        }
        
    }
    
}
