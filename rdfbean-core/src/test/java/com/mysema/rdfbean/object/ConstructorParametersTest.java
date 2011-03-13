/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Default;
import com.mysema.rdfbean.annotations.InjectService;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
@Ignore
public class ConstructorParametersTest {

    @ClassMapping(ns = TEST.NS)
    public static final class ChildType {
        @Predicate
        @Default(ns = TEST.NS)
        final ParentType parent;

        public ChildType(ParentType parent) {
            this.parent = parent;
        }
    }

    @ClassMapping(ns = TEST.NS)
    public static final class ParentType {
        public ParentType(
                @InjectService @Default(ns = TEST.NS, ln = "parentService") ParentServiceType service) {
            assertNotNull(service);
        }
    }

    @ClassMapping(ns = TEST.NS)
    public static final class ParentServiceType {

    }

    @Test
    public void ConstructorInjection() {
        Session session = SessionUtil.openSession(ChildType.class, ParentType.class, ParentServiceType.class);
        session.addParent(TEST.NS, session);
        ChildType child = session.getBean(ChildType.class, new UID(TEST.NS,"child"));
        assertNotNull(child);
        assertNotNull(child.parent);
    }

}
