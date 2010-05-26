package com.mysema.rdfbean.spring;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class RDFBeanTransactionManagerTest {
    
    private static final ApplicationContext appContext = new ClassPathXmlApplicationContext("/persistence.xml");
    
    private DemoService demoService;
    
    @Before
    public void setUp(){
        demoService = (DemoService) appContext.getBean("demoService");
    }
    
    @Test
    public void testAsserts(){	
	demoService.assertWriteTx();
	demoService.assertReadTx();
	demoService.assertUnbound();
    }
    
    @Test(expected=Exception.class)
    public void testRollback() throws Exception{
        demoService.rollback();
        // TODO : assert that transaction was actually rolled back
    }

}
