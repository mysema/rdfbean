/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.InjectProperty;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.UID;

public class EnumTest {

    @ClassMapping(ns=TEST.NS)
    public static enum EnumType{
        PRIMARY,
        SECONDARY;
    }

    @ClassMapping(ns=TEST.NS)
    public static class EnumRef {
        @Predicate
        EnumType etype;
        public EnumRef(@InjectProperty("etype") EnumType etype) {
            this.etype = etype;
        }
    }
    
    @Test
    public void testEnum() {
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, EnumType.class, EnumRef.class);
        session.save(new EnumRef(EnumType.SECONDARY));
        
        session = SessionUtil.openSession(repository, EnumType.class, EnumRef.class);
        EnumRef eref = session.findInstances(EnumRef.class).get(0);
        assertEquals(EnumType.SECONDARY, eref.etype);
    }
    
    @Test
    public void getId(){
        MiniRepository repository = new MiniRepository();
        Session session = SessionUtil.openSession(repository, EnumType.class, EnumRef.class);        
        assertEquals(new UID(TEST.NS, "PRIMARY"), session.getId(EnumType.PRIMARY));
        assertEquals(new UID(TEST.NS, "SECONDARY"), session.getId(EnumType.SECONDARY));
    }
}
