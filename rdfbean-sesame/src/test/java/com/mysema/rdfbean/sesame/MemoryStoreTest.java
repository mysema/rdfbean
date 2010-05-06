package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;

public class MemoryStoreTest {
    
    private static final String DATA_DIR = "target/MemoryStoreTest";

    @ClassMapping(ns=TEST.NS,ln="MemoryStoreTest_Item")
    public static class Item {
	
	@Id(IDType.RESOURCE)
	private ID resource;
	
	@Predicate(ln="path")
	private String path;

	public String getPath() {
            return path;
        }

	public void setPath(String id) {
            this.path = id;
        }

	public ID getResource() {
            return resource;
        }

	public void setResource(ID resource) {
            this.resource = resource;
        }
	
    }
    
    private SessionFactoryImpl sessionFactory;
    
    @Before
    public void setUp() throws IOException{
	Configuration configuration = new DefaultConfiguration(Item.class);
        MemoryRepository repository = new MemoryRepository();
        if (new File(DATA_DIR).exists()){
            FileUtils.cleanDirectory(new File(DATA_DIR));    
        }        
        repository.setSesameInference(false);
        repository.setDataDirName(DATA_DIR);
        
        sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
    }
    
    @After
    public void tearDown(){
	sessionFactory.close();
    }
    
    @Test
    public void test() throws IOException{       
	Session session = sessionFactory.openSession();
        RDFBeanTransaction tx = session.beginTransaction();        
        
        Item item = new Item();
        item.setPath("xxx");
        session.save(item);
        tx.commit();
        session.close();
        
        session = sessionFactory.openSession();
        QMemoryStoreTest_Item itemVar = QMemoryStoreTest_Item.item;
        try{
            assertEquals(1, session.from(itemVar).list(itemVar).size());
            Item i = session.from(itemVar).list(itemVar).get(0);
            assertNotNull(i);
            assertEquals("xxx", i.getPath());
            
            assertNotNull(session.from(itemVar).where(itemVar.path.eq("xxx")).uniqueResult(itemVar));    
        }finally{
            session.close();
        }
    }

}
