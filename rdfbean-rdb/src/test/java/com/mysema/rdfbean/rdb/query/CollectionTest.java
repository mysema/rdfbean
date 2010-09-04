/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb.query;

import org.junit.Test;

import com.mysema.query.alias.Alias;

import static com.mysema.query.alias.Alias.*;
import static org.junit.Assert.*;

import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Company;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Department;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Employee;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Company.class, Department.class, Employee.class})
public class CollectionTest extends AbstractRDBTest implements CompanyDepartmentEmployeeDomain{
    
    @Test
    public void test(){        
        Department dep1 = new Department();
        session.saveAll(dep1, new Department());
        
        Employee emp1 = new Employee();
        emp1.department = dep1;
        Employee emp2 = new Employee();
        Employee emp3 = new Employee();
        Employee emp4 = new Employee();
        session.saveAll(emp1, emp2, emp3, emp4);
        
        Department dep = Alias.alias(Department.class);
        Employee emp = Alias.alias(Employee.class);
        assertEquals(1l, session.from($(dep)).where($(dep.getEmployees()).contains(emp1)).count());
        assertEquals(3l, session.from($(emp)).where($(emp).in(emp1, emp2, emp3)).count());
    }

}
