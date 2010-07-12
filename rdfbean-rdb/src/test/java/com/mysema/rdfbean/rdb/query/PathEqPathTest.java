package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.domains.EntityDomain.Entity;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.TestConfig;

@TestConfig({Entity.class})
public class PathEqPathTest extends AbstractRDBTest implements EntityDomain{
    
    private Session session;
    
    @Test
    public void test(){
        Entity entity = new Entity();
        entity.text1 = "a";
        entity.text2 = "a";
        
        Entity entity2 = new Entity();
        entity2.text1 = "a";
        entity2.text2 = "b";
        
        session.saveAll(entity, entity2);
        
        Entity var = Alias.alias(Entity.class);
        assertEquals(1l, session.from($(var)).where($(var.getText1()).eq($(var.getText2()))).count());
        
    }

}
