package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static com.mysema.query.alias.Alias.alias;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.domains.ContextDomain;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ ContextDomain.Entity1.class, ContextDomain.Entity2.class, ContextDomain.Entity3.class })
public class ContextTest extends SessionTestBase implements ContextDomain {

    private static final Entity1 e1 = alias(Entity1.class);

    private static final Entity2 e2 = alias(Entity2.class);

    private static final Entity3 e3 = alias(Entity3.class);

    @Before
    public void setUp() {
        Entity1 entity1 = new Entity1();
        entity1.property = "X";
        Entity2 entity2 = new Entity2();
        entity2.property = "X";
        Entity3 entity3 = new Entity3();
        entity3.property = "X";

        session.save(entity1);
        session.save(entity2);
        session.save(entity3);
        session.flush();
    }

    @Test
    public void Counts() {
        assertEquals(1, session.from($(e1)).where($(e1.getProperty()).isNotNull()).list($(e1)).size());
        assertEquals(1, session.from($(e2)).where($(e2.getProperty()).isNotNull()).list($(e2)).size());
        assertEquals(1, session.from($(e3)).where($(e3.getProperty()).isNotNull()).list($(e3)).size());
    }

}
