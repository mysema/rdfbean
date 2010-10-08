/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Localized;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
/**
 * @author sasa
 *
 */
public class LocalizationTest {
    
    private static final Locale EN = new Locale("en");
    
    private static final Locale FI = new Locale("fi");
    
    protected static List<Locale> locales = Arrays.asList(FI, EN);
    
    @ClassMapping(ns = RDFS.NS, ln="Resource")
    public final static class LocalizedMapTest{
        @Predicate(ln="label")
        @Localized
        private Map<Locale, String> labels;
        public Map<Locale, String> getLabels() {
            return this.labels;
        }
    }
    
    @Test
    public void LocalizedMap() {
        UID subject = new UID(TEST.NS, "LocalizedMapTest");
        MiniRepository repository = new MiniRepository(
                new STMT(subject, RDFS.label, new LIT("suomeksi", FI)),
                new STMT(subject, RDFS.label, new LIT("in english", EN))
        );
        
        Session session = SessionUtil.openSession(repository, locales, LocalizedMapTest.class);
        LocalizedMapTest lmaptest = session.getBean(LocalizedMapTest.class, subject);
        assertNotNull(lmaptest.getLabels());
        assertEquals("suomeksi", lmaptest.getLabels().get(FI));
        assertEquals("in english", lmaptest.getLabels().get(EN));
    }

}
