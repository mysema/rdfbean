package com.mysema.rdfbean.object;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.domains.ListDomain;

public class InterfacePolymorphismTest {

    private DefaultConfiguration configuration;
    
    @Before
    public void setUp(){
        configuration = new DefaultConfiguration();
        configuration.addClasses(
                ListDomain.Identifiable.class,
                ListDomain.Elements.class,
                ListDomain.Element.class,
                ListDomain.LinkElement.class,
                ListDomain.TextElement.class);
    }
    
    @Test
    public void Identifiable_Is_Polymorphic(){
        assertTrue(configuration.isPolymorphic(ListDomain.Identifiable.class));
    }
    
    @Test
    public void Elements_Isnt_Polymorphic(){
        assertFalse(configuration.isPolymorphic(ListDomain.Elements.class));
    }
    
    @Test
    public void Element_Is_Polymorphic(){
        assertTrue(configuration.isPolymorphic(ListDomain.Element.class));
    }
    
    @Test
    public void LinkElement_Isnt_Polymorphic(){
        assertFalse(configuration.isPolymorphic(ListDomain.LinkElement.class));
    }
    
    @Test
    public void TextElement_Isnt_Polymorphic(){
        assertFalse(configuration.isPolymorphic(ListDomain.TextElement.class));
    }
    
}
