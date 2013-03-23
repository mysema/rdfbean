/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Container;
import com.mysema.rdfbean.annotations.ContainerType;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;

@ClassMapping
public class ContainerTest {

    @Id(IDType.RESOURCE)
    ID id;

    @Predicate
    @Container(ContainerType.SEQ)
    List<String> names = new ArrayList<String>();

    @Test
    public void Container() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, ContainerTest.class);

        names.add("Eka");
        names.add(null);
        names.add("Toka");
        session.save(this);

        CloseableIterator<STMT> iter = repository.findStatements(null, RDF.type, RDF.Seq, null, false);
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());

        iter = repository.findStatements(null, RDF.getContainerMembershipProperty(1), null, null, false);
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());

        session = SessionUtil.openSession(repository, ContainerTest.class);
        ContainerTest test = session.findInstances(ContainerTest.class).get(0);
        assertEquals("Eka", test.names.get(0));
        assertNull(test.names.get(1));
        assertEquals("Toka", test.names.get(2));
    }

}
