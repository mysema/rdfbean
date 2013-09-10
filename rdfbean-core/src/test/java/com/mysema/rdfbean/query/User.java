/**
 * 
 */
package com.mysema.rdfbean.query;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Localized;
import com.mysema.rdfbean.annotations.MapElements;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;

@ClassMapping(ns = TEST.NS)
public class User {

    enum Gender {
        MALE, FEMALE
    }

    @Id
    ID id;

    @Predicate
    String firstName;

    @Predicate
    String lastName;

    @Predicate(ln = "buddy")
    Set<User> buddies;

    @Predicate
    List<User> buddyList;

    @Predicate
    @Localized
    String name;

    @Predicate(ln = "name")
    @Localized
    Map<Locale, String> names;

    @Predicate(ln = "buddy")
    @MapElements(key = @Predicate(ln = "firstName"))
    Map<String, User> buddiesMapped;

    @Predicate
    Gender gender;

    public User() {
    }

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Set<User> getBuddies() {
        return buddies;
    }

    public void setBuddies(Set<User> buddies) {
        this.buddies = buddies;
    }

    public Map<Locale, String> getNames() {
        return names;
    }

    public void setNames(Map<Locale, String> names) {
        this.names = names;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

}