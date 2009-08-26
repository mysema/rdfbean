/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.guice;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Provider;
import com.google.inject.matcher.AbstractMatcher;
import com.mysema.rdfbean.guice.tx.NotTransactional;
import com.mysema.rdfbean.guice.tx.Propagation;
import com.mysema.rdfbean.guice.tx.Transactional;

/**
 * TransactionalMethodMatcher provides
 *
 * @author tiwe
 * @version $Id$
 */
class TransactionalMethodMatcher extends AbstractMatcher<Method> implements Provider<Map<Method,Transactional>>{

    private final Map<Method,Transactional> configuration = new HashMap<Method,Transactional>();
    
    @Override
    public boolean matches(Method method) {
        // annotated method
        if (method.getAnnotation(Transactional.class) != null){
            return handle(method, method.getAnnotation(Transactional.class));
            
        // annotated class
        }else if (method.getDeclaringClass().getAnnotation(Transactional.class) != null){
            if (method.getAnnotation(NotTransactional.class) == null){
                return handle(method, method.getDeclaringClass().getAnnotation(Transactional.class));   
            }else{
                return false;
            }
        }
        
        for (Class<?> iface : method.getDeclaringClass().getInterfaces()){            
            for (Method m : iface.getMethods()){
                // annotated interface method
                if (m.getName().equals(method.getName())
                  && m.getAnnotation(Transactional.class) != null
                  && equals(m.getParameterTypes(), method.getParameterTypes())){
                    return handle(method, m.getAnnotation(Transactional.class));
                }
            }
        }
        return false;
    }
    
    private boolean handle(Method method, Transactional annotation) {
        boolean intercepted = annotation.propagation() != Propagation.SUPPORTS;        
        if (intercepted){
            configuration.put(method, annotation);
        }
        return intercepted;
    }

    private boolean equals(Object[] a, Object[] b) {
        if (a.length == b.length){
            return Arrays.asList(a).equals(Arrays.asList(b));
        }
        return false;
    }

    @Override
    public Map<Method, Transactional> get() {
        return configuration;
    }

}
