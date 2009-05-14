/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;


/**
 * SailQueryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SimpleQueriesTest extends AbstractSesameQueryTest{
         
    @Test
    public void allIds(){
        System.out.println("allIds");
        instances = newQuery().from(var).list(var);
        List<String> ids = Arrays.asList(instances.get(0).id, instances.get(1).id);        
        assertEquals(ids, newQuery().from(var).list(var.id)); 
    }
    
    @Test
    public void allInstances(){
        System.out.println("allInstances");
        instances = newQuery().from(var).list(var);
        assertEquals(2, instances.size());
        for (TestType i : instances){
            System.out.println(i.id + ", " + i.directProperty);
        }        
    }
    
    @Test
    public void allDistinctInstances(){
        System.out.println("allDistinctInstances");
        assertEquals(2, newQuery().from(var).listDistinct(var).size());
    }
    
    @Test
    public void byId(){
        System.out.println("byId");
        String id = newQuery().from(var).list(var.id).get(0);
        instance = where(var.id.eq(id)).uniqueResult(var);
        assertNotNull(instance);
        assertEquals(id, instance.id);
    }
    
    @Test
    public void byIdNegated(){
        System.out.println("byIdNegated");
        instances = newQuery().from(var).list(var);
        instance = where(var.id.ne(instances.get(0).id)).uniqueResult(var);
        assertNotNull(instance);
        assertEquals(instances.get(1).id, instance.id);
    }
    
    @Test
    public void byLiteralProperty(){
        System.out.println("byLiteralProperty");
        instance = where(var.directProperty.eq("propertymap")).uniqueResult(var);
        assertNotNull(instance);
        assertEquals("propertymap", instance.directProperty);
    }
    
    @Test
    public void byNonExistantProperty(){
        System.out.println("byNonExistantProperty");
        assertEquals(2, where(var.notExistantProperty.isnull()).list(var).size());        
        assertEquals(0, where(var.notExistantProperty.isnotnull()).list(var).size());
    }
    
    @Test
    public void byNumericProperty(){
        assertEquals(1, where(var.numericProperty.eq(10)).list(var).size());
        assertEquals(1, where(var.numericProperty.eq(20)).list(var).size());
        assertEquals(0, where(var.numericProperty.eq(30)).list(var).size());
    }
    
    @Test
    @Ignore
    public void byReferenceProperty(){
        // TODO        
    }
    
    @Test
    public void idAndDirectProperties(){        
        System.out.println("idAndDirectProperties");
        newQuery().from(var).list(var.id, var.directProperty);        
    }
 
    @Test
    public void typeOf(){
        System.out.println("typeOf");
        assertEquals(2, where(var.typeOf(TestType.class)).list(var).size());
    }
    
    @Test
    public void listAccess(){        
        System.out.println("listAccess");
        assertEquals(1, where(var.listProperty(1).directProperty.eq("nsprefix")).list(var).size());
    }
    
    @Test
    public void listAccess2(){
        System.out.println("listAccess2");
        assertEquals(1, where(var.listProperty(0).directProperty.eq("target_idspace")).list(var).size());
    }
        
    @Test
    @Ignore
    public void mapAccess(){
        // TODO
    }
    
    @Test
    @Ignore
    public void dtoProjection(){
     // TODO
    }
    
    @Test
    public void matchLocale(){
        System.out.println("matchLocale");
        assertEquals(1, where(var.localizedProperty.eq("fi")).list(var).size());
        assertEquals(1, where(var.localizedProperty.eq("en")).list(var).size());
    }
    
    @Test
    public void matchLocale2(){
        System.out.println("matchLocale2");
        assertEquals(1, where(var.localizedProperty.ne("fi")).list(var).size());
        assertEquals(1, where(var.localizedProperty.ne("en")).list(var).size());
    }
    
    @Test
    public void localizedMap(){
        System.out.println("localizedMap");
        assertEquals(1, where(var.localizedAsMap.get(new Locale("fi")).eq("fi")).list(var).size());
        assertEquals(1, where(var.localizedAsMap.get(new Locale("en")).eq("fi")).list(var).size());
        assertEquals(0, where(var.localizedAsMap.get(new Locale("")).eq("fi")).list(var).size());
    }
    
    @Test
    public void localizedMap2(){
        System.out.println("localizedMap2");
        assertEquals(1, where(var.localizedAsMap.get(new Locale("en")).eq("en")).list(var).size());
        assertEquals(1, where(var.localizedAsMap.get(new Locale("fi")).eq("en")).list(var).size());
        assertEquals(0, where(var.localizedAsMap.get(new Locale("")).eq("en")).list(var).size());
    }
    
}
