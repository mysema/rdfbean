/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.lucene.Searchable;
import com.mysema.rdfbean.lucene.SearchablePredicate;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.DefaultConfiguration;

/**
 * ComponentTypesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ComponentTypesTest extends AbstractConfigurationTest{
    
    @Searchable(embeddedOnly=true)
    @ClassMapping(ns=TEST.NS)
    public static class Tag{
        @Predicate
        @SearchablePredicate(text=true)
        String name;
        
        public Tag(){}
        
        public Tag(String name){
            this.name = name;
        }
    }

    @Test
    public void componentTypes(){
        configuration.setCoreConfiguration(new DefaultConfiguration(Tag.class));        
        configuration.initialize();    
        
        assertTrue(configuration.getComponentTypes().contains(new UID(TEST.NS, "Tag")));
    }
}
