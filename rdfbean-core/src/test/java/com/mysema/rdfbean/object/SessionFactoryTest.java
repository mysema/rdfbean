/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;

/**
 * SessionFactoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SessionFactoryTest {
    
    private SessionFactoryImpl sessionFactory;
    
    @After
    public void tearDown(){
        sessionFactory.close();
    }
    
    @Test
    public void test() throws Exception{
        Configuration configuration = new DefaultConfiguration();        
        Repository repository = new MiniRepository();
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        
        Field field = SessionFactoryImpl.class.getDeclaredField("model");
        field.setAccessible(true);
        
        BID model1 = (BID) field.get(sessionFactory);        
        sessionFactory.close();
        
        sessionFactory.initialize();
        BID model2 = (BID) field.get(sessionFactory);
        assertEquals(model1, model2);
        
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        
        model2 = (BID) field.get(sessionFactory);
        assertEquals(model1, model2);
    }

}
