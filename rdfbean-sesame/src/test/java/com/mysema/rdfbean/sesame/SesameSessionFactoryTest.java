/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;

import org.junit.Test;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.SessionFactoryImpl;

/**
 * SesameSessionFactoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SesameSessionFactoryTest {
    
    @Test
    public void test() throws StoreException, RDFParseException, IOException{
        MemoryRepository repository = new MemoryRepository();
        repository.setSources(
                new RDFSource("classpath:/test.ttl", Format.TURTLE, TEST.NS),
                new RDFSource("classpath:/foaf.rdf", Format.RDFXML, FOAF.NS)
        );
        repository.initialize();
        
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration());
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        
//        assertTrue(sessionFactory.getCurrentSession() == null);
//        assertTrue(SessionFactoryUtils.createSession(sessionFactory));
//        assertTrue(sessionFactory.getCurrentSession() != null);
//        assertTrue(SessionFactoryUtils.releaseSession(sessionFactory.getCurrentSession(), sessionFactory));
//        assertTrue(sessionFactory.getCurrentSession() == null);
    }

}
