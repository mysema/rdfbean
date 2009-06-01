/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;

import org.junit.Test;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.TEST;
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
        MemoryStore store = new MemoryStore();
        Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(store));            
        repository.initialize();
        RepositoryConnection connection = repository.getConnection();
        if (connection.isEmpty()) {
            ClassLoader classLoader = SessionTestBase.class.getClassLoader();
            ValueFactory vf = connection.getValueFactory();
            connection.add(classLoader.getResourceAsStream("test.ttl"), TEST.NS, RDFFormat.TURTLE, vf.createURI(TEST.NS));
            connection.add(classLoader.getResourceAsStream("foaf.rdf"), FOAF.NS, RDFFormat.RDFXML, vf.createURI(FOAF.NS));
        }
        connection.close();
        
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setDefaultConfiguration(new DefaultConfiguration());
        sessionFactory.setRepository(new SesameRepository(repository));
        
//        assertTrue(sessionFactory.getCurrentSession() == null);
//        assertTrue(SessionFactoryUtils.createSession(sessionFactory));
//        assertTrue(sessionFactory.getCurrentSession() != null);
//        assertTrue(SessionFactoryUtils.releaseSession(sessionFactory.getCurrentSession(), sessionFactory));
//        assertTrue(sessionFactory.getCurrentSession() == null);
    }

}
