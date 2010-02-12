package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.SessionFactoryImpl;

/**
 * SessionFactoryTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SessionFactoryTest {
    
    @Test
    public void test() throws Exception{
        Configuration configuration = new DefaultConfiguration();        
        Repository repository = new MemoryRepository();
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
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
    }

}
