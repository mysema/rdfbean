package com.mysema.rdfbean.object;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.Inject;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

public class PropertyOverrideTest {
    
    public static class WithinClass {
        @Predicate(ns=TEST.NS)
        private WithinClass reference;
        
        @Inject
        public WithinClass getReference() {
            return reference;
        }
    }

    @Test
    public void overrideWithinClass() {
        MappedClass mappedClass = MappedClass.getMappedClass(WithinClass.class);
        MappedPath path = mappedClass.getMappedPath("reference");
        assertEquals(new UID(TEST.NS, "reference"), path.get(0).uid());
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
