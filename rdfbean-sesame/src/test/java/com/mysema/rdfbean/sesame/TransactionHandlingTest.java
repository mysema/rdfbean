/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFParseException;

import com.mysema.query.types.PathMetadataFactory;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.Restriction;

public class TransactionHandlingTest extends SessionTestBase{

    private static final Locale FI = new Locale("fi");

    private final EntityPathBase<Restriction> restriction = new EntityPathBase<Restriction>(
            Restriction.class,
            PathMetadataFactory.forVariable("var"));

    private SessionFactoryImpl sessionFactory;

    private Session session;

    @Before
    public void setUp() throws RDFParseException, IOException{
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration(OWL.class.getPackage()));
        sessionFactory.setRepository(repository);
        sessionFactory.setLocales(Collections.singleton(FI));
        sessionFactory.initialize();
    }

    @Override
    @After
    public void tearDown() throws IOException{
        if (session != null){
            session.close();
        }
    }

    @Test
    public void Commit() throws ClassNotFoundException, IOException{
        session = sessionFactory.openSession();
        int count = session.from(restriction).list(restriction).size();
        RDFBeanTransaction tx = session.beginTransaction();
        session.save(new Restriction());
        session.save(new Restriction());
        tx.commit();
        session.close();

        session = sessionFactory.openSession();
        assertEquals(count + 2, session.from(restriction).list(restriction).size());
    }

    @Test
    public void Rollback() throws ClassNotFoundException, IOException{
        session = sessionFactory.openSession();
        int count = session.from(restriction).list(restriction).size();
        RDFBeanTransaction tx = session.beginTransaction();
        session.save(new Restriction());
        session.save(new Restriction());
        tx.rollback();
        session.close();

        session = sessionFactory.openSession();
        assertEquals(count, session.from(restriction).list(restriction).size());
    }

}
