package com.mysema.rdfbean.spring;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import com.mysema.rdfbean.TEST;


public class ContextAwareSessionFactoryTest {
    
    @Test
    public void test(){
	ContextAwareSessionFactory sessionFactory = new ContextAwareSessionFactory(TEST.NS);
	ApplicationContext appContext = new StaticApplicationContext();
	sessionFactory.setApplicationContext(appContext);
	// TODO : asserts
    }

}
