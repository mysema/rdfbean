package com.mysema.rdfbean.guice.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.transaction.annotation.NotTransactional;
import org.springframework.transaction.annotation.Transactional;

import com.google.inject.Inject;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * TxTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@Transactional
@RunWith(GuiceTestRunner.class)
public class TxTest {

    @Inject
    private SessionFactory sessionFactory;
    
    @Test
    public void tx(){
        assertNotNull(sessionFactory.getCurrentSession());
    }
    
    @Test
    @NotTransactional
    public void noTx(){
        assertNull(sessionFactory.getCurrentSession());
    }
}
