/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.domains.EntityDomain.Entity;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

/**
 * PathEqPathTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@SessionConfig(Entity.class)
public class PathEqPathTest extends SessionTestBase implements EntityDomain {
    
    @Test
    public void test() throws StoreException{
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
