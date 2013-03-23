/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.domains.EntityDomain.Entity;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ Entity.class })
public class PathEqPathTest extends AbstractRDBTest implements EntityDomain {

    @Test
    public void test() {
        Entity entity = new Entity();
        entity.text1 = "a";
        entity.text2 = "a";

        Entity entity2 = new Entity();
        entity2.text1 = "a";
        entity2.text2 = "b";

        session.saveAll(entity, entity2);

        Entity e1 = Alias.alias(Entity.class, "e1");
        Entity e2 = Alias.alias(Entity.class, "e2");
        assertEquals(1l, session.from($(e1)).where($(e1.getText1()).eq($(e1.getText2()))).count());
        assertEquals(2l, session.from($(e1), $(e2)).where($(e1.getText1()).eq($(e2.getText1())), $(e1).ne($(e2))).count());

    }

}
