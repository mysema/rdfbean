package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.STMT;
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
    
    private SessionFactoryImpl sessionFactory;
    
    @After
    public void tearDown(){
        sessionFactory.close();
    }
    
    @Test
    public void withMiniRepository() throws Exception{
        test(new MiniRepository());
    }
    
    @Test
    public void withNativeRepository() throws Exception{
        File dataDir = new File("target/test-repo1");
        FileUtils.deleteDirectory(dataDir);
        NativeRepository repository = new NativeRepository();
        repository.setDataDir(dataDir);
        test(repository);
    }
    
    @Test
    @Ignore
    public void withMemoryRepository() throws Exception{
        File dataDir = new File("target/test-repo2");
        FileUtils.deleteDirectory(dataDir);
        MemoryRepository repository = new MemoryRepository();
        repository.setDataDir(dataDir);
        test(repository);
    }
        
    private void test(Repository repository) throws Exception{
        Configuration configuration = new DefaultConfiguration();  
        
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        assertTrue(checkModelIdStmts(repository));
        
        Field field = SessionFactoryImpl.class.getDeclaredField("model");
        field.setAccessible(true);
        
        BID model1 = (BID) field.get(sessionFactory);        
        sessionFactory.close();        
        
        sessionFactory.initialize();
        assertTrue(checkModelIdStmts(repository));
        BID model2 = (BID) field.get(sessionFactory);
        assertEquals(model1, model2);
        
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        assertTrue(checkModelIdStmts(repository));
        
        model2 = (BID) field.get(sessionFactory);
        assertEquals(model1, model2);
    }
    
    private boolean checkModelIdStmts(Repository repository) throws IOException{
        RDFConnection connection = repository.openConnection();
        try{
            CloseableIterator<STMT> stmts = connection.findStatements(null, CORE.modelId, null, null, false);
            try{
                return stmts.hasNext();
            }finally{
                stmts.close();
            }
        }finally{
            connection.close();
        }        
    }

}
