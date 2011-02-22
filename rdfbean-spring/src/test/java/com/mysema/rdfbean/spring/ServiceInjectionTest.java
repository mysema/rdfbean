/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;

import com.mysema.rdfbean.SRV;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Default;
import com.mysema.rdfbean.annotations.InjectService;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

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
        @InjectService
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
        @InjectService
        ServiceInterface mixinService;
    }

    private static StaticApplicationContext applicationContext = new StaticApplicationContext();

    static {
        applicationContext.registerSingleton("helloWorld", HelloWorldService.class);
        applicationContext.registerSingleton("helloUnderWorld", HelloUnderWorldService.class);
    }

    @Test
    public void InjectSpringServices() throws ClassNotFoundException {
        BID subject = new BID("foobar");
        MiniRepository repository = new MiniRepository(
                new STMT(subject, RDF.type, new UID(TEST.NS, "RichBean")),
                new STMT(subject, RDFS.label, new LIT("RichBean"))
        );

        Session session = SessionUtil.openSession(repository, RichBean.class);
        session.addParent(SRV.NS, new SpringObjectRepository(applicationContext));
        RichBean rbean = session.findInstances(RichBean.class).get(0);
        assertEquals("Hello RichBean!", rbean.executeService());

        session.clear();

        // Override default value
        repository.add(new STMT(subject, new UID(TEST.NS, "service"), new UID(SRV.NS, "helloUnderWorld")));
        rbean = session.findInstances(RichBean.class).get(0);
        assertEquals("Welcome to the Underworld, RichBean!", rbean.executeService());
    }

    @Test
    @Ignore
    public void MixinInjection() {
        UID uid = new UID(SRV.NS, "helloWorld");
        Session session = SessionUtil.openSession(MixinInjection.class);
        session.addParent(SRV.NS, new SpringObjectRepository(applicationContext));
        MixinInjection mixin = session.getBean(MixinInjection.class, uid);
        assertNotNull(mixin);
        assertEquals(applicationContext.getBean("helloWorld"), mixin.mixinService);
    }
}
