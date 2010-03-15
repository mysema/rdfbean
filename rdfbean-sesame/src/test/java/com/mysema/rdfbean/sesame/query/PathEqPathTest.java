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
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * PathEqPathTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PathEqPathTest extends SessionTestBase{

    @ClassMapping(ns=TEST.NS)
    public static class Entity{
        @Predicate
        String text1;
        
        @Predicate
        String text2;

        public String getText1() {
            return text1;
        }

        public String getText2() {
            return text2;
        }
        
    }
    
    @Test
    public void test() throws StoreException{
        session = createSession(Entity.class);
        
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
