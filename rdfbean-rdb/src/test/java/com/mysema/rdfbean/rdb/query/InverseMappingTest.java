package com.mysema.rdfbean.rdb.query;

import org.junit.Test;

import static com.mysema.query.alias.Alias.*;
import static org.junit.Assert.*;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Company;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Department;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Employee;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({Company.class, Department.class, Employee.class})
public class InverseMappingTest extends AbstractRDBTest implements CompanyDepartmentEmployeeDomain{
    
    @Test
    public void test(){
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
                
        Company c = Alias.alias(Company.class);
        Department d = Alias.alias(Department.class);
        Employee e = Alias.alias(Employee.class);
        
        // count instances
        assertEquals(1l, session.from($(c)).count());
        assertEquals(1l, session.from($(d)).count());
        assertEquals(1l, session.from($(e)).count());
        
        // direct
        assertEquals(1l, session.from($(d)).where($(d.getCompany()).eq(company)).count());
        assertEquals(1l, session.from($(d)).where($(d.getCompany()).eq(company)).count());
        assertEquals(1l, session.from($(e)).where($(e.getDepartment()).eq(department)).count());
        
        // inverse 
        assertEquals(1l, session.from($(c)).where($(c.getDepartment()).eq(department)).count());
        assertEquals(1l, session.from($(c)).where($(c.getDepartments()).contains(department)).count());
        assertEquals(1l, session.from($(d)).where($(d.getEmployees()).contains(employee)).count());
        
    }

}
