/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.LID;

public class MixinTest {

    @ClassMapping
    public static final class AType {
        @Mixin
        BType asBType;
        @Id(IDType.LOCAL)
        LID id;
    }

    @ClassMapping
    public static final class BType {
        @Mixin
        AType asAType;
        @Id(IDType.LOCAL)
        LID id;
    }

    private Session session;

    private ID id;

    @Before
    public void setUp() {
        session = SessionUtil.openSession(AType.class, BType.class);
        id = session.save(new AType());
        session.flush();
        session.clear();
    }

    @Test
    public void SelfReferenceTest() {
        AType atype = session.get(AType.class, id);
        assertNotNull(atype);
        assertEquals(session.getLID(id), atype.id);
        assertNotNull(atype.asBType);
        assertEquals(session.getLID(id), atype.asBType.id);
    }

}
