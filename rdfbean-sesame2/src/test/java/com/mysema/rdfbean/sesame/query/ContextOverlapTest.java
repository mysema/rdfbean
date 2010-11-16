package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Context;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ContextOverlapTest.Entity1.class, ContextOverlapTest.Entity2.class, ContextOverlapTest.Entity3.class})
public class ContextOverlapTest extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS,ln="E")    
    public static class Entity1 {     
        @Id
        String id;        
    }
    
    @ClassMapping(ns=TEST.NS,ln="E")
    @Context(TEST.NS)
    public static class Entity2 {        
        @Id
        String id;                
    }
    
    @ClassMapping(ns=TEST.NS,ln="E")
    @Context("http://www.example.com/")
    public static class Entity3 {        
        @Id
        String id;        
    }
    
    @Before
    public void setUp(){
        session.save(new Entity1());
        session.save(new Entity2());
        session.save(new Entity3());
        
    }
    
    @Test
    public void Find_Instances_Of_Types_In_Different_Contexts(){
        assertEquals(3, session.findInstances(Entity1.class).size());
        assertEquals(2, session.findInstances(Entity2.class).size()); // because inferred triples are queried as well
        assertEquals(2, session.findInstances(Entity3.class).size()); // because inferred triples are queried as well
    }

}
