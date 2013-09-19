package com.mysema.rdfbean.object;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.MiniRepository;

public class LocalIdTest {

    @ClassMapping
    public static class Entity {
        
        @Id
        ID id;
        
        @Predicate
        String name;
    }
    
    @Test
    public void Persistence() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, Entity.class);
        Entity entity = new Entity();
        entity.name = "John Doe";
        session.save(entity);
        session.flush();
        
        assertFalse(repository.findStatements(null, CORE.localId, null, null, false).hasNext());
    }
}
