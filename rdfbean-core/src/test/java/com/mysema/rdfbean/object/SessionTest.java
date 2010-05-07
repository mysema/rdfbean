/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;

/**
 * SessionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SessionTest {
    
    private Long value = Long.valueOf(0l);
    
    private Session session = SessionUtil.openSession();
    
    @Test(expected=IllegalArgumentException.class)
    public void save(){
        session.save(value);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void saveAll(){
        session.saveAll(value, Long.valueOf(1l));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void delete(){
        session.delete(value);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void deleteAll(){
        session.deleteAll(value, Long.valueOf(0l));
    }

    @Test(expected=IllegalArgumentException.class)
    public void getId(){
        session.getId(value);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void findUnknown(){
//        launchpad bug : #576846
        session.findInstances(SessionTest.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void findUnknown2(){
//        launchpad bug : #576846
        PathBuilder<SessionTest> entity = new PathBuilder<SessionTest>(SessionTest.class, "var");
        session.from(entity).list(entity);
    }
    
}
