package com.mysema.rdfbean.domains;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Context;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;

public interface ContextDomain {

    String NS1 = "http://www.example.com/ns1#";

    String NS2 = "http://www.example.com/ns2#";

    String NS3 = "http://www.example.com/ns3#";

    @ClassMapping
    @Context(NS1)
    public static class Entity1 {

        @Id
        String id;

        @Predicate
        public String property;

        @Predicate
        public Entity2 entity;

        public String getProperty() {
            return property;
        }

        public Entity2 getEntity() {
            return entity;
        }

    }

    @ClassMapping
    public static class Entity2 {

        @Id
        String id;

        @Predicate(context = NS2)
        public String property;

        @Predicate
        public Entity3 entity;

        public String getProperty() {
            return property;
        }

        public Entity3 getEntity() {
            return entity;
        }

    }

    @ClassMapping
    public static class Entity3 {

        @Id
        String id;

        @Predicate(context = NS3)
        public String property;

        @Predicate
        public Entity1 entity;

        public String getProperty() {
            return property;
        }

        public Entity1 getEntity() {
            return entity;
        }

    }
}
