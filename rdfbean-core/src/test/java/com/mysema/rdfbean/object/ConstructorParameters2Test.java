/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.InjectProperty;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;

/**
 * @author sasa
 * 
 */
public class ConstructorParameters2Test {

    @ClassMapping(ns = TEST.NS)
    public static final class Child {
        @Id(IDType.RESOURCE)
        final ID id;
        
        @Predicate
        final Parent parent;

        public Child(@InjectProperty("id") ID id, @InjectProperty("parent") Parent parent) {
            this.id = id;
            this.parent = parent;
        }
    }

    @ClassMapping(ns = TEST.NS)
    public static final class Parent {
        @Id(IDType.RESOURCE)
        ID id;
    }

    @Test
    public void constructorInjection() {
        Session session = SessionUtil.openSession(Child.class, Parent.class);
        Parent parent = new Parent();
        Child child = new Child(new BID(), parent);
        session.saveAll(parent, child);
        session.flush();
        session.clear();
        
        Child child2 = session.get(Child.class, child.id);
        assertNotNull(child2);
        assertEquals(child.id, child2.id);
        assertEquals(child.parent.id, child2.parent.id);
    }

}
