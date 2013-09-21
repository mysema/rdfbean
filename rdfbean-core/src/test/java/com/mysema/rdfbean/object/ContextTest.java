package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class ContextTest {

    @ClassMapping
    public static class Entity {

        @Id(IDType.LOCAL)
        String id;

        @Predicate
        String property1;

        @Predicate(context = TEST.NS)
        String property2;
        
        @Predicate
        String property3;

    }

    private MiniRepository repository;

    private Session session;

    @Before
    public void setUp() {
        repository = new MiniRepository();
        session = SessionUtil.openSession(repository, Entity.class);
        Entity entity = new Entity();
        entity.property1 = "X";
        entity.property2 = "Y";
        entity.property3 = "Z";
        session.save(entity);
        session.flush();
        session.clear();
    }

    @Test
    public void findInstances() {
        Entity entity = session.findInstances(Entity.class).get(0);
        assertEquals("X", entity.property1);
        assertEquals("Y", entity.property2);
    }

    @Test
    public void findStatements() {
        List<STMT> property1 = IteratorAdapter.asList(repository.findStatements(null, new UID(TEST.NS, "property1"), null, null, false));
        assertEquals(1, property1.size());
        assertNull(property1.get(0).getContext());

        List<STMT> property2 = IteratorAdapter.asList(repository.findStatements(null, new UID(TEST.NS, "property2"), null, null, false));
        assertEquals(1, property2.size());
        assertEquals(new UID(TEST.NS), property2.get(0).getContext());
        
        List<STMT> property3 = IteratorAdapter.asList(repository.findStatements(null, new UID(TEST.NS, "property3"), null, null, false));
        assertEquals(1, property3.size());
        assertNull(property3.get(0).getContext());
    }

}
