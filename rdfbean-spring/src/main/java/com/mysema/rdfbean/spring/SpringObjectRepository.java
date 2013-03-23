/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.spring;

import javax.annotation.concurrent.Immutable;

import org.springframework.beans.factory.BeanFactory;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.ObjectRepository;

/**
 * @author sasa
 * 
 */
@Immutable
public class SpringObjectRepository implements ObjectRepository {

    private final BeanFactory beanFactory;

    public SpringObjectRepository(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz, UID subject) {
        return (T) beanFactory.getBean(subject.ln(), clazz);
    }

}
