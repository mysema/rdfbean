/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Default;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public class AutowireTest {

    @ClassMapping
    public final static class DomainType<T> {

        @Predicate(ns=RDF.NS, ln="type")
        Class<T> parametrizedClass;

        @Id(IDType.URI)
        String uri = TEST.NS + "domainType";
    }

    public static class Command {

        @Default(ns=TEST.NS, ln="domainType")
        DomainType<DomainType<?>> domainType;

    }

    @Test
    public void ClassReference() {
        MiniRepository repository = new MiniRepository();
        repository.add(
            new STMT(new UID(TEST.NS, "domainType"), RDF.type, new UID(TEST.NS, "DomainType"))
        );
        Session session = SessionUtil.openSession(repository, DomainType.class);
        Command command = new Command();
        assertNull(command.domainType);

        session.autowire(command);
        assertNotNull(command.domainType);
        assertNotNull(command.domainType.parametrizedClass);
        assertTrue(DomainType.class.isAssignableFrom(command.domainType.parametrizedClass));
    }

}
