package com.mysema.rdf.demo.domain;

import java.util.LinkedHashSet;
import java.util.Set;

import com.mysema.commons.lang.Assert;
import com.mysema.rdf.demo.DEMO;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.annotations.Required;

@ClassMapping(ns=DEMO.NS)
public class Company extends Party {
    
    private String officialName;
    
    @Predicate(ln="company", inv=true)
    private Set<Person> employees = new LinkedHashSet<Person>();

    public Company() {
        // Java Bean constructor
    }

    @Predicate
    public String getOfficialName() {
        return officialName;
    }

    @Required
    public void setOfficialName(String officialName) {
        this.officialName = Assert.hasText(officialName);
    }
    
    public Set<Person> getEmployees() {
        return employees;
    }

    public void addEmployee(Person employee) {
        employee.setCompany(this);
        this.employees.add(employee);
    }

    @Override
    public String getDisplayName() {
        return officialName;
    }
}
