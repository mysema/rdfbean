/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;

public interface UserDepartmentCompanyDomain {

    @ClassMapping
    public static class User {

        @Id(IDType.LOCAL)
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

    @ClassMapping
    public static class Department {

        @Id(IDType.LOCAL)
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

    @ClassMapping
    public static class Company {

        @Id(IDType.LOCAL)
        public String id;

        public String getId() {
            return id;
        }

    }

}
