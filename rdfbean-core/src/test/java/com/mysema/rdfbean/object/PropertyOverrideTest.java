/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.InjectService;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

public class PropertyOverrideTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class WithinClass {
        @Predicate(ns=TEST.NS)
        private WithinClass reference;
        
        @InjectService
        public WithinClass getReference() {
            return reference;
        }
    }

    @Test
    public void overrideWithinClass() {
        Configuration configuration = new DefaultConfiguration(WithinClass.class);
        MappedClass mappedClass = configuration.getMappedClass(WithinClass.class);
        MappedPath path = mappedClass.getMappedPath("reference");
        assertEquals(new UID(TEST.NS, "reference"), path.get(0).getUID());
        MappedProperty<?> property = path.getMappedProperty();
        assertTrue(property.isInjection());
    }

    public void overrideSuperClassProperties() {
        // TODO
    }
    
    public void illegalOverride() {
        // TODO
    }
}
