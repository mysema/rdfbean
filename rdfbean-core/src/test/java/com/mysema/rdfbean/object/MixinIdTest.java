package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.model.IDType;

public class MixinIdTest {

    @ClassMapping
    public static final class AType {
        @Mixin
        BType asBType = new BType();
        @Id(IDType.LOCAL)
        String id;
    }

    @ClassMapping
    public static final class BType {
        @Id(IDType.LOCAL)
        String id;
    }

    private Session session;

    @Before
    public void setUp() {
        session = SessionUtil.openSession(AType.class, BType.class);
    }

    @Test
    public void B_Id_Is_Set() {
        AType atype = new AType();
        session.save(atype);
        assertNotNull(atype.id);
        assertEquals(atype.id, atype.asBType.id);
    }

}
