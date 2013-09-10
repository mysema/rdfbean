package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;

public class ConstructorParameters4Test {

    @ClassMapping
    public static class Entity {

        @Id
        final ID id;

        @Predicate
        final String firstName;

        @Predicate
        final String lastName;

        public Entity(ID id, @Predicate(ln = "firstName") String fn, @Predicate(ln = "lastName") String ln) {
            this.id = id;
            this.firstName = fn;
            this.lastName = ln;
        }

    }

    @Test
    public void ConstructorInjection() {
        Session session = SessionUtil.openSession(Entity.class);
        Entity entity = new Entity(new BID(), "Jon", "Doe");
        session.save(entity);
        session.clear();

        entity = session.get(Entity.class, entity.id);
        assertEquals("Jon", entity.firstName);
        assertEquals("Doe", entity.lastName);

    }
}
