package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Company;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Department;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Employee;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Company.class, Department.class, Employee.class})
public class CollectionTest extends SessionTestBase implements CompanyDepartmentEmployeeDomain{
    
    private Employee emp1, emp2, emp3, emp4;
    
    @Before
    public void setUp(){
        Department dep1 = new Department();
        session.saveAll(dep1, new Department());
        
        emp1 = new Employee();
        emp1.department = dep1;
        emp2 = new Employee();
        emp3 = new Employee();
        emp4 = new Employee();
        session.saveAll(emp1, emp2, emp3, emp4);         
    }
    
    @Test
    public void Contains(){               
        Department dep = Alias.alias(Department.class);
        assertEquals(1l, session.from($(dep)).where($(dep.getEmployees()).contains(emp1)).count());
    }
    
    @Test
    public void In(){
        Employee emp = Alias.alias(Employee.class);
        assertEquals(3l, session.from($(emp)).where($(emp).in(emp1, emp2, emp3)).count());
    }
    
}
