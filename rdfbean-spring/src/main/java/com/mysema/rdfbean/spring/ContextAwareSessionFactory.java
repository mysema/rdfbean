/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.spring;

import java.util.Collections;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.SRV;
import com.mysema.rdfbean.object.ObjectRepository;

/**
 * ContextAwareSessionFactory provides
 *
 * @author tiwe
 * @version $Id$
 */
public final class ContextAwareSessionFactory extends SpringSessionFactory implements ApplicationContextAware{
    
    private String namespace = SRV.NS;
    
    public ContextAwareSessionFactory() {}
    
    public ContextAwareSessionFactory(String namespace) {
        setNamespace(namespace);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        setObjectRepositories(Collections.<String,ObjectRepository>singletonMap(
                namespace, 
                new SpringObjectRepository(applicationContext)));
        
    }

    public void setNamespace(String namespace) {
        this.namespace = Assert.hasText(namespace);
    }
    
}
