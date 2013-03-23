package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

@ClassMapping
public class IdMappingTest {

    @Id(value = IDType.URI, ns = "http://example.com/")
    String id;

    @Test
    public void Success() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, IdMappingTest.class);

        IdMappingTest instance = new IdMappingTest();
        instance.id = "abc";
        session.save(instance);
        session.clear();

        UID id = new UID("http://example.com/abc");
        assertTrue(repository.exists(id, null, null, null));
        IdMappingTest instance2 = session.get(IdMappingTest.class, id);
        assertEquals(instance.id, instance2.id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Error() {
        MiniRepository repository = new MiniRepository();
        UID id = new UID("http://example2.com/abc");
        UID type = new UID(TEST.NS, "IdMappingTest");
        repository.add(new STMT(id, RDF.type, type));

        Session session = SessionUtil.openSession(repository, IdMappingTest.class);
        session.get(IdMappingTest.class, id);
    }

}
