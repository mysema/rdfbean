package com.mysema.rdfbean.domain;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;

@ClassMapping(ns = DEMO.NS)
public class Person extends Party {

    @Predicate
    private String firstName;

    @Predicate
    private String lastName;

    @Predicate
    private int age;

    @Predicate
    private Company company;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    @Override
    public String getDisplayName() {
        return "" + firstName + " " + lastName;
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
