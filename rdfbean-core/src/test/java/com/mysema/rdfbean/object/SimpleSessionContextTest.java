package com.mysema.rdfbean.object;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.MiniRepository;

/**
 * SimpleSessionContextTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SimpleSessionContextTest {
    
    private SimpleSessionContext sessionContext;

    @Before
    public void setUp(){
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration());
        sessionFactory.setRepository(new MiniRepository());
        sessionFactory.initialize();
        sessionContext = new SimpleSessionContext(sessionFactory);
    }
    
    @Test
    public void testGetCurrentSession() {
        assertNull(sessionContext.getCurrentSession());
    }

    @Test
    public void testGetOrCreateSession() {
        assertNull(sessionContext.getCurrentSession());
        assertNotNull(sessionContext.getOrCreateSession());
        assertNotNull(sessionContext.getCurrentSession());
    }

    @Test
    public void testReleaseSession() {
        assertNull(sessionContext.getCurrentSession());
        assertNotNull(sessionContext.getOrCreateSession());
        assertNotNull(sessionContext.getCurrentSession());
        sessionContext.releaseSession();
        assertNull(sessionContext.getCurrentSession());
    }

}
