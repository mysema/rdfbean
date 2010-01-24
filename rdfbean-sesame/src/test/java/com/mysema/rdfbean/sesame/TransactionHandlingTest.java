/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PathMetadataFactory;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.Restriction;

/**
 * TransactionHandlingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class TransactionHandlingTest extends SessionTestBase{
    
    private static final Locale FI = new Locale("fi");
    
    private PEntity<Restriction> restriction = new PEntity<Restriction>(
            Restriction.class, 
            PathMetadataFactory.forVariable("var"));
    
    private SessionFactoryImpl sessionFactory;
    
    @Before
    public void setUp() throws StoreException, RDFParseException, IOException{
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration(OWL.class.getPackage()));
        sessionFactory.setRepository(repository);
        sessionFactory.setLocales(Collections.singleton(FI));
        sessionFactory.initialize();
    }
    
    @Test
    public void commit() throws StoreException, ClassNotFoundException, IOException{
        session = sessionFactory.openSession();
        int count = session.from(restriction).list(restriction).size();
        RDFBeanTransaction tx = session.beginTransaction();
        session.save(new Restriction());
        session.save(new Restriction());
        tx.commit();
        session.close();
        
        session = createSession(FI, OWL.class.getPackage());
        assertEquals(count + 2, session.from(restriction).list(restriction).size());
    }
        
    @Test
    public void rollback() throws StoreException, ClassNotFoundException, IOException{
        session = createSession(FI, OWL.class.getPackage());
        int count = session.from(restriction).list(restriction).size();
        RDFBeanTransaction tx = session.beginTransaction();
        session.save(new Restriction());
        session.save(new Restriction());
        tx.rollback();
        session.close();
        
        session = createSession(FI, OWL.class.getPackage());
        assertEquals(count, session.from(restriction).list(restriction).size());       
    }

}
