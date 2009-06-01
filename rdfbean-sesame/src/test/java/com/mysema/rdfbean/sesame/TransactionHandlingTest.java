/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.QRestriction;
import com.mysema.rdfbean.owl.Restriction;

/**
 * TransactionHandlingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class TransactionHandlingTest extends SessionTestBase{
    
    private static final Locale FI = new Locale("fi");
    
    private QRestriction restriction = QRestriction.restriction;
    
    private SesameSessionFactory sessionFactory;
    
    @Before
    public void setUp() throws StoreException, RDFParseException, IOException{
        sessionFactory = new SesameSessionFactory();
        sessionFactory.setDefaultConfiguration(new DefaultConfiguration(OWL.class.getPackage()));
        sessionFactory.setRepository(repository);
    }
    
    @Test
    public void commit() throws StoreException, ClassNotFoundException{
        Session session = SessionUtil.openSession(newRDFConnection(), FI, OWL.class.getPackage());
        int count = session.from(restriction).list(restriction).size();
        RDFBeanTransaction tx = session.beginTransaction();
        session.save(new Restriction());
        session.save(new Restriction());
        tx.commit();
        
        session = createSession(FI, OWL.class.getPackage());
        assertEquals(count + 2, session.from(restriction).list(restriction).size());
    }
        
    @Test
    public void rollback() throws StoreException, ClassNotFoundException{
        Session session = createSession(FI, OWL.class.getPackage());
        int count = session.from(restriction).list(restriction).size();
        RDFBeanTransaction tx = session.beginTransaction();
        session.save(new Restriction());
        session.save(new Restriction());
        tx.rollback();
        
        session = createSession(FI, OWL.class.getPackage());
        assertEquals(count, session.from(restriction).list(restriction).size());
    }

}
