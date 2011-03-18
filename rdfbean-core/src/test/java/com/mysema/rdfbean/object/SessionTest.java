/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.annotations.ClassMapping;

public class SessionTest {

    @ClassMapping
    static class EntityWithoutId{

    }

    private final Long value = Long.valueOf(0l);

    private final Session session = SessionUtil.openSession(EntityWithoutId.class);

    @Test(expected=IllegalArgumentException.class)
    public void Save(){
        session.save(value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void SaveAll(){
        session.saveAll(value, Long.valueOf(1l));
    }

    @Test(expected=IllegalArgumentException.class)
    public void Delete(){
        session.delete(value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void DeleteAll(){
        session.deleteAll(value, Long.valueOf(0l));
    }

    @Test(expected=IllegalArgumentException.class)
    public void GetId(){
        session.getId(value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void FindUnknown(){
//        launchpad bug : #576846
        session.findInstances(SessionTest.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void FindUnknown2(){
//        launchpad bug : #576846
        PathBuilder<SessionTest> entity = new PathBuilder<SessionTest>(SessionTest.class, "var");
        session.from(entity).list(entity);
    }

    @Test(expected=IllegalArgumentException.class)
    public void SaveEntityWithoutId(){
//        #576836
        session.save(new EntityWithoutId());
    }

    @Test(expected=IllegalArgumentException.class)
    public void SaveEntityWithoutId2(){
//        #576836
        session.saveAll(new EntityWithoutId());
    }

    @Test
    public void FindInstancesWithoutId(){
        session.findInstances(EntityWithoutId.class);
    }

}
