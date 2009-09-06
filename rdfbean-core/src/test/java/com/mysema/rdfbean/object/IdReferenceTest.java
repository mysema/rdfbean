/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.UID;
/**
 * @author sasa
 *
 */
public class IdReferenceTest {

    @ClassMapping(ns=TEST.NS)
    public final static class IDResource {
        @Id(IDType.RESOURCE)
        ID id;
    }

    @ClassMapping(ns=TEST.NS)
    public final static class LIDResource {
        @Id(IDType.LOCAL)
        LID id;
    }
    
    @Test
    public void uri() {
        Session session = SessionUtil.openSession(IDResource.class);
        IDResource resource1 = session.getBean(IDResource.class, new UID(TEST.NS, "localResource"));
        assertNotNull(resource1);
        
        ID id = resource1.id;
        assertNotNull(id);
        assertTrue(id instanceof UID);
        assertEquals(new UID(TEST.NS, "localResource"), id);
        
        session.clear();
        IDResource resource2 = session.getBean(IDResource.class, new UID(TEST.NS, "localResource"));
        assertTrue(resource1 != resource2);
        assertEquals(id, resource2.id);
    }
    
    @Test
    public void bnode(){
        Session session = SessionUtil.openSession(IDResource.class);
        LID lid = session.getLID(new BID("foobar"));
        IDResource resource1 = session.get(IDResource.class, lid);
        assertNotNull(resource1);
        
        ID id = resource1.id;
        assertNotNull(id);
        assertTrue(id instanceof BID);
        assertEquals(new BID("foobar"), id);
    }
    
    @Test
    public void local(){
        Session session = SessionUtil.openSession(LIDResource.class);
        LID lid = session.getLID(new BID("foobar"));
        LIDResource resource1 = session.get(LIDResource.class, lid);
        assertNotNull(resource1);
        
        LID id = resource1.id;
        assertNotNull(id);
//        assertTrue(id instanceof LID);
        assertTrue(Integer.parseInt(id.getId()) > 0);
    }
}
