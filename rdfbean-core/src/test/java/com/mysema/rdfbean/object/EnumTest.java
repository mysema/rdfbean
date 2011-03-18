/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.UID;

public class EnumTest {

    @ClassMapping
    public static enum EnumType{
        PRIMARY,
        SECONDARY;
    }

    @ClassMapping
    public static class EnumRef {

        @Id(IDType.RESOURCE)
        ID id;

        @Predicate
        EnumType etype;

        public EnumRef(EnumType etype) {
            this.etype = etype;
        }
    }

    @Test
    public void Enum() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, EnumType.class, EnumRef.class);
        session.save(new EnumRef(EnumType.SECONDARY));

        session = SessionUtil.openSession(repository, EnumType.class, EnumRef.class);
        EnumRef eref = session.findInstances(EnumRef.class).get(0);
        assertEquals(EnumType.SECONDARY, eref.etype);
    }

    @Test
    public void getId(){
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, EnumType.class, EnumRef.class);
        assertEquals(new UID(TEST.NS, "PRIMARY"), session.getId(EnumType.PRIMARY));
        assertEquals(new UID(TEST.NS, "SECONDARY"), session.getId(EnumType.SECONDARY));
    }
}
