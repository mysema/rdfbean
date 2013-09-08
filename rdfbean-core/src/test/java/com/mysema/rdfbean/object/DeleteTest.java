/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Container;
import com.mysema.rdfbean.annotations.ContainerType;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;

@ClassMapping
public class DeleteTest {

    @Id(IDType.LOCAL)
    private String id;

    @Predicate
    private String name;

    @Predicate
    private final List<DeleteTest> listReference = new ArrayList<DeleteTest>();

    @Predicate
    @Container(ContainerType.SEQ)
    private final List<DeleteDTO> seqReference = new ArrayList<DeleteDTO>();

    @Nullable
    private Session session;

    private final MiniRepository repository = new MiniRepository();

    @ClassMapping(ns = TEST.NS, ln = "DeleteTest")
    public static class DeleteDTO {
        @Id(IDType.LOCAL)
        String id;

        public DeleteDTO() {
        }

        public DeleteDTO(String id) {
            this.id = id;
        }
    }

    @Before
    public void init() {
        this.session = newSession();
    }

    @After
    public void close() throws IOException {
        this.session.close();
    }

    @Test
    public void SimpleDelete() {
        DeleteTest dtest = new DeleteTest();
        dtest.name = "dtest";

        session.save(dtest);
        newSession();

        DeleteTest tmp = dtest;
        dtest = session.getById(dtest.id, DeleteTest.class);
        assertNotSame(tmp, dtest);
        assertEquals("dtest", dtest.name);

        session.delete(dtest);

        assertNull(session.getById(dtest.id, DeleteTest.class));

        newSession();
        assertNull(session.getById(dtest.id, DeleteTest.class));
    }

    @Test
    public void SimpleDeleteResource() {
        DeleteTest dtest = new DeleteTest();
        dtest.name = "dtest";

        session.save(dtest);
        newSession();

        DeleteTest tmp = dtest;
        dtest = session.getById(dtest.id, DeleteTest.class);
        assertNotSame(tmp, dtest);
        assertEquals("dtest", dtest.name);

        session.delete(DeleteTest.class, session.getId(dtest));

        assertNull(session.getById(dtest.id, DeleteTest.class));

        newSession();
        assertNull(session.getById(dtest.id, DeleteTest.class));
    }

    @Test
    public void RemoveReferences() {
        DeleteTest dtest1 = new DeleteTest();
        dtest1.name = "dtest1";

        DeleteTest dtest2 = new DeleteTest();
        dtest2.name = "dtest2";
        dtest2.listReference.add(dtest1);

        // Circular reference
        dtest1.listReference.add(dtest2);

        session.save(dtest1);
        String id1 = dtest1.id;
        String id2 = dtest2.id;
        assertNotNull(id1);
        assertNotNull(id2);

        newSession();
        dtest1 = session.getById(id1, DeleteTest.class);
        dtest2 = dtest1.listReference.get(0);
        assertEquals(id2, dtest2.id);
        assertSame(dtest1, dtest2.listReference.get(0));

        dtest1.seqReference.add(new DeleteDTO(id2));
        dtest2.seqReference.add(new DeleteDTO(id1));
        dtest2.seqReference.add(new DeleteDTO(id2)); // self reference

        session.save(dtest1); // cascades to dtest2
        session.delete(dtest1);
        assertNull(session.getById(id1, DeleteTest.class));

        newSession();
        assertNull(session.getById(id1, DeleteTest.class));
        dtest2 = session.getById(id2, DeleteTest.class);
        assertNotNull(dtest2);
        assertNull(dtest2.listReference.get(0));
        assertNull(dtest2.seqReference.get(0));
        assertEquals(id2, dtest2.seqReference.get(1).id);
    }

    private Session newSession() {
        closeSession();
        session = SessionUtil.openSession(repository, DeleteTest.class, DeleteDTO.class);
        return session;
    }

    private void closeSession() {
        if (session != null) {
            session.close();
            session = null;
        }
    }
}
