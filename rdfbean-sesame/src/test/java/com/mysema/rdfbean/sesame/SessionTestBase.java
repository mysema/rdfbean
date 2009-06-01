/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.BeforeClass;
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
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * @author sasa
 *
 */
public class SessionTestBase {
    
    protected static Locale FI = new Locale("fi");

    protected static Locale EN = new Locale("en");
    
    protected static List<Locale> locales = Arrays.asList(FI, EN);
    
    protected static Repository repository;

    @BeforeClass
    public static void setup() throws StoreException, RDFParseException, IOException, ClassNotFoundException {
        if (repository == null) {
            MemoryStore store = new MemoryStore();
            repository = new SailRepository(new ForwardChainingRDFSInferencer(store));         
            repository = new SailRepository(new ForwardChainingRDFSInferencer(store));
            repository.initialize();
            RepositoryConnection connection = repository.getConnection();
            if (connection.isEmpty()) {
                ClassLoader classLoader = SessionTestBase.class.getClassLoader();
                ValueFactory vf = connection.getValueFactory();
                connection.add(classLoader.getResourceAsStream("test.ttl"), TEST.NS, RDFFormat.TURTLE, vf.createURI(TEST.NS));
                connection.add(classLoader.getResourceAsStream("foaf.rdf"), FOAF.NS, RDFFormat.RDFXML, vf.createURI(FOAF.NS));
            }
            connection.close();
        }
    }

    protected static Session createSession(Package... packages) throws StoreException, ClassNotFoundException {
        return SessionUtil.openSession(newRDFConnection(), locales, packages);
    }

    protected static Session createSession(Locale locale, Package... packages) throws StoreException, ClassNotFoundException {
        return SessionUtil.openSession(newRDFConnection(), locale, packages);
    }

    protected static Session createSession(Class<?>... classes) throws StoreException {
        return SessionUtil.openSession(newRDFConnection(), locales, classes);
    }

    protected static Session createSession(Locale locale, Class<?>... classes) throws StoreException {
        return SessionUtil.openSession(newRDFConnection(), locale, classes);
    }
    
    protected static SesameConnection newRDFConnection() throws StoreException {
        return new SesameConnection(newDefaultConnection());
    }
    
    protected static RepositoryConnection newDefaultConnection() throws StoreException {
        return repository.getConnection();
    }
}
