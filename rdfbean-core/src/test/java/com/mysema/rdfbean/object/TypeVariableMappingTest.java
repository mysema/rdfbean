/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.annotations.Required;


public class TypeVariableMappingTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class Parent<
            T extends Parent<?, ?, ?>, 
            P, 
            E extends Parent<?, ?, ?>> {
        @Predicate
        T parent;
        @Predicate
        P property;
        @Predicate
        Collection<E> children;
        @Predicate
        String parentProperty;
    }

    @ClassMapping(ns=TEST.NS)
    public static class FirstChild extends Parent<FirstChild, Integer, FirstChild> {
    }

    @ClassMapping(ns=TEST.NS)
    public static class SecondChild<C extends SecondChild<C>> extends Parent<C, String, C> {
        @Required
        public String getProperty() {
            return property;
        }
    }

    @ClassMapping(ns=TEST.NS)
    public static class NestedChild extends SecondChild<NestedChild> {
    }
    
    @Test
    public void testMappings() {
        Configuration configuration = new DefaultConfiguration(Parent.class, FirstChild.class, NestedChild.class, SecondChild.class);
        
        MappedClass mappedClass = configuration.getMappedClass(Parent.class);

        // Parent
        MappedPath path = mappedClass.getMappedPath("parent");
        assertEquals(Parent.class, path.getMappedProperty().getType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("property");
        assertEquals(Object.class, path.getMappedProperty().getType());
        assertFalse(path.getMappedProperty().isRequired());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("children");
        assertEquals(Parent.class, path.getMappedProperty().getComponentType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("parentProperty");
        assertFalse(path.isInherited());
        
        // FirstChild
        mappedClass = configuration.getMappedClass(FirstChild.class);
        path = mappedClass.getMappedPath("parent");
        assertEquals(FirstChild.class, path.getMappedProperty().getType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("property");
        assertEquals(Integer.class, path.getMappedProperty().getType());
        assertFalse(path.getMappedProperty().isRequired());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("children");
        assertEquals(FirstChild.class, path.getMappedProperty().getComponentType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("parentProperty");
        assertTrue(path.isInherited());

        // NestedChild
        mappedClass = configuration.getMappedClass(NestedChild.class);
        path = mappedClass.getMappedPath("parent");
        assertEquals(NestedChild.class, path.getMappedProperty().getType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("property");
        assertEquals(String.class, path.getMappedProperty().getType());
        assertTrue(path.getMappedProperty().isRequired());
        assertTrue(path.isInherited());
        
        path = mappedClass.getMappedPath("children");
        assertEquals(NestedChild.class, path.getMappedProperty().getComponentType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("parentProperty");
        assertTrue(path.isInherited());

        // SecondChild
        mappedClass = configuration.getMappedClass(SecondChild.class);
        path = mappedClass.getMappedPath("parent");
        assertEquals(SecondChild.class, path.getMappedProperty().getType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("property");
        assertEquals(String.class, path.getMappedProperty().getType());
        assertTrue(path.getMappedProperty().isRequired());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("children");
        assertEquals(SecondChild.class, path.getMappedProperty().getComponentType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("parentProperty");
        assertTrue(path.isInherited());

        // No changes to Parent
        mappedClass = configuration.getMappedClass(Parent.class);
        path = mappedClass.getMappedPath("parent");
        assertEquals(Parent.class, path.getMappedProperty().getType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("property");
        assertEquals(Object.class, path.getMappedProperty().getType());
        assertFalse(path.getMappedProperty().isRequired());
        
        path = mappedClass.getMappedPath("children");
        assertEquals(Parent.class, path.getMappedProperty().getComponentType());
        assertFalse(path.isInherited());
        
        path = mappedClass.getMappedPath("parentProperty");
        assertFalse(path.isInherited());
    }
    
}
