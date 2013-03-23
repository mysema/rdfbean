package com.mysema.rdf.demo.domain;

import com.mysema.commons.lang.Assert;
import com.mysema.rdf.demo.DEMO;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.annotations.Required;

@ClassMapping(ns = DEMO.NS)
public class Person extends Party {

    @Predicate
    @Required
    private String firstName;

    @Predicate
    @Required
    private String lastName;

    @Predicate
    private int age;

    @Predicate
    private Company company;

    // public String serviceResult() {
    // return demoService.sayHello();
    // }

    public Person(String firstName, String lastName) {
        this.firstName = Assert.hasText(firstName, "firstName");
        this.lastName = Assert.hasText(lastName, "lastName");
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Company getCompany() {
        return company;
    }

    void setCompany(Company company) {
        this.company = company;
    }

}
