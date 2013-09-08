package com.mysema.rdfbean.domains;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.annotations.Properties;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.UID;

public interface PropertiesDomain {

    UID _project = new UID(TEST.NS, "1");
    UID _person = new UID(TEST.NS, "2");
    UID _owner = new UID(TEST.NS, "owner");
    UID _name = new UID(TEST.NS, "name");
    UID _created = new UID(TEST.NS, "created");
    UID _description = new UID(TEST.NS, "description");
    UID _creatorComment = new UID(TEST.NS, "creatorComment");
    UID _deadline = new UID(TEST.NS, "deadline");

    @ClassMapping
    public static class Person {

        @Id(IDType.LOCAL)
        public String id;

        @Predicate
        public String name;
    }

    @ClassMapping
    public static class Iteration {

        @Id(IDType.LOCAL)
        String id;

        @Predicate
        String name;
    }

    @ClassMapping
    public static class Project {

        @Id(IDType.LOCAL)
        public String id;

        @Predicate
        public String name;

        @Predicate
        public LocalDate created;

        @Properties(includeMapped = true)
        public Map<UID, LocalDate> dates;

        @Properties
        public Map<UID, Person> participants;

        @Properties
        public Map<UID, Iteration> iterations;

        @Properties
        public Map<UID, Set<String>> infos;

        public Project() {
        }

        public Project(String name) {
            this.name = name;
        }
    }

    @ClassMapping
    public static class InvalidProject1 {

        @Properties
        public Map<UID, NODE> starter;

        @Properties
        public Map<UID, NODE> invalid;
    }

    @ClassMapping
    public static class InvalidProject2 {
        @Properties
        public List<UID> nodes;
    }

    @ClassMapping
    public static class InvalidProject3 {
        @Properties
        public Map<String, String> nodes;
    }

}
