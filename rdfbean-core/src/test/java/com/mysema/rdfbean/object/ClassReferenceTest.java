/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Default;
import com.mysema.rdfbean.model.UID;

public class ClassReferenceTest {

    @ClassMapping(ns=TEST.NS)
    public final static class ClassReference {
        @Default(ns=TEST.NS, ln="ClassReference")
        Class<Object> type;
    }
    
    @Test
    public void ClassReference() {
        Session session = SessionUtil.openSession(ClassReference.class);
        ClassReference cref = session.getBean(ClassReference.class, new UID(TEST.NS, "foo"));
        assertEquals(ClassReference.class, cref.type);
    }
    
}
