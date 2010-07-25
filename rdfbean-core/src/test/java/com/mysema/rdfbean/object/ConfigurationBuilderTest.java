package com.mysema.rdfbean.object;

import org.junit.Test;

import com.mysema.rdfbean.TEST;

public class ConfigurationBuilderTest {
    
    public static class Person {
        
        String id;
        
        String firstName, lastName;
        
        Person superior;
        
        Department department;
    }
    
    public static class Department {
        
        String id;
        
        String name;
        
        Company company;
    }
    
    public static class Company {
        
        String id;
        
        String name;
    }
    
    @Test
    public void test(){
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(TEST.NS, Person.class).addId("id").addProperties();
        builder.addClass(TEST.NS, "Dept", Department.class).addId("id").addProperties();
        builder.addClass(TEST.NS, Company.class).addId("id").addProperties();
        Configuration configuration = builder.build();
        
        // person
        MappedClass person = configuration.getMappedClass(Person.class);
        MappedPath person_id = person.getMappedPath("id");
        MappedPath person_firstName = person.getMappedPath("firstName");
        MappedPath person_lastName = person.getMappedPath("lastName");
        MappedPath person_superior = person.getMappedPath("superior");
        MappedPath person_department = person.getMappedPath("department");
        // department
        MappedClass department = configuration.getMappedClass(Department.class);
        MappedPath department_id = department.getMappedPath("id");
        MappedPath department_name = department.getMappedPath("name");
        MappedPath department_company = department.getMappedPath("company");
        // company
        MappedClass company = configuration.getMappedClass(Company.class);
        MappedPath company_id = company.getMappedPath("id");
        MappedPath company_name = company.getMappedPath("name");
    }

}
