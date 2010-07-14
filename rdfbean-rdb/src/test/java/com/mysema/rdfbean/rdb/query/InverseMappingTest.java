package com.mysema.rdfbean.rdb.query;

import org.junit.Test;

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
        
        // TODO : queries        
    }

}
