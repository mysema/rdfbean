package com.mysema.rdfbean.object;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Properties;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class DynamicPropertiesContextTest {
    
    @ClassMapping
    static class Entity {
        
        @Id
        ID id;
        
        @Properties
        Map<UID, ID> properties;
        
        @Properties(context=TEST.NS)
        Map<UID, ID> properties2;
    }
    
    @Test
    public void test() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, Entity.class);
        
        UID testContext = new UID(TEST.NS);
        UID sibling = new UID(TEST.NS, "sibling");
        UID parent = new UID(TEST.NS, "parent");
        
        // save
        Entity entity = new Entity();
        entity.properties = Maps.newHashMap();
        entity.properties2 = Maps.newHashMap();
        entity.properties.put(sibling, new BID());
        entity.properties2.put(parent, new BID());
        session.save(entity);
        session.flush();
        
        // repository state
        STMT siblingStmt = repository.findStatements(entity.id, sibling, null, null, false).next();
        STMT parentStmt = repository.findStatements(entity.id, parent, null, null, false).next();
        assertNull(siblingStmt.getContext());
        assertEquals(testContext, parentStmt.getContext());
        
        // load
        session.clear();
        entity = session.get(Entity.class, entity.id);
        assertEquals(2, entity.properties.size()); // XXX by design
        assertEquals(1, entity.properties2.size());
        assertTrue(entity.properties.containsKey(sibling));
        assertTrue(entity.properties2.containsKey(parent));
    }

}
