package com.mysema.rdfbean.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.query.types.path.SimplePath;
import com.mysema.rdfbean.domains.IdentityDomain;
import com.mysema.rdfbean.domains.IdentityDomain.Entity1;
import com.mysema.rdfbean.domains.IdentityDomain.Entity2;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ Entity1.class, Entity2.class })
public class IdentityTest extends SessionTestBase implements IdentityDomain {

    @Test
    public void test() {
        Entity1 entity1 = new Entity1();
        Entity2 entity2 = new Entity2();
        session.save(entity1);
        session.save(entity2);

        Entity1 e1 = Alias.alias(Entity1.class);
        Entity2 e2 = Alias.alias(Entity2.class);
        SimplePath<ID> idPath = new SimplePath<ID>(ID.class, $(e1), "id");
        assertEquals(entity1, session.from($(e1)).where(idPath.eq(entity1.getId())).uniqueResult($(e1)));
        assertEquals(entity2, session.from($(e2)).where($(e2.getId()).eq(entity2.getId())).uniqueResult($(e2)));
    }

}