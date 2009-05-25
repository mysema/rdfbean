/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.spring;

import static com.mysema.rdfbean.model.MiniDialect.LIT;
import static com.mysema.rdfbean.model.MiniDialect.STMT;
import static com.mysema.rdfbean.model.MiniDialect.UID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;

import com.mysema.rdfbean.SRV;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Default;
import com.mysema.rdfbean.annotations.Inject;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.MiniSession;
import com.mysema.rdfbean.object.Session;

/**
 * @author sasa
 *
 */
public class ServiceInjectionTest {
    
    public static interface ServiceInterface {
        public String doYourThing(RichBean rbean);
    }
    
    public static class HelloWorldService implements ServiceInterface {
        public String doYourThing(RichBean rbean) {
            return "Hello " + rbean.getLabel()+"!";
        }
    }
    
    public static class HelloUnderWorldService implements ServiceInterface {
        public String doYourThing(RichBean rbean) {
            return "Welcome to the Underworld, " + rbean.getLabel()+"!";
        }
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class RichBean {
        @Predicate(ns=RDFS.NS)
        private String label;

        @Predicate // Optional for Default mapped property - allows overriding default
        @Default(ns=SRV.NS, ln="helloWorld")
        @Inject
        private ServiceInterface service;
        
        public String executeService() {
            return service.doYourThing(this);
        }
        public String getLabel() {
            return this.label;
        }
    }
    
    @ClassMapping(ns=TEST.NS)
    public final static class MixinInjection {
        @Mixin
        @Inject
        ServiceInterface mixinService;
    }

    private static StaticApplicationContext applicationContext = new StaticApplicationContext();

    static {
        applicationContext.registerSingleton("helloWorld", HelloWorldService.class);
        applicationContext.registerSingleton("helloUnderWorld", HelloUnderWorldService.class);
    }
    
    @Test
    public void injectSpringServices() throws ClassNotFoundException {
        BID subject = new BID("foobar");
        MiniRepository repository = new MiniRepository(
                STMT(subject, RDF.type, UID(TEST.NS, "RichBean")),
                STMT(subject, RDFS.label, LIT("RichBean"))
        );

        Session session = new MiniSession(repository, RichBean.class);
        session.addParent(SRV.NS, new SpringObjectRepository(applicationContext));
        RichBean rbean = session.findInstances(RichBean.class).get(0);
        assertEquals("Hello RichBean!", rbean.executeService());
        
        session.clear();
        
        // Override default value
        repository.add(STMT(subject, UID(TEST.NS, "service"), UID(SRV.NS, "helloUnderWorld")));
        rbean = session.findInstances(RichBean.class).get(0);
        assertEquals("Welcome to the Underworld, RichBean!", rbean.executeService());
    }

    @Test
    public void mixinInjection() {
        UID uid = new UID(SRV.NS, "helloWorld");
        Session session = new MiniSession(MixinInjection.class);
        session.addParent(SRV.NS, new SpringObjectRepository(applicationContext));
        MixinInjection mixin = session.getBean(MixinInjection.class, uid);
        assertNotNull(mixin);
        assertEquals(applicationContext.getBean("helloWorld"), mixin.mixinService);
    }
}
