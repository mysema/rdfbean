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

/**
 * TransactionalMethodMatcher provides
 *
 * @author tiwe
 * @version $Id$
 */
class TransactionalMethodMatcher extends AbstractMatcher<Method> 
    implements Provider<Map<Method,Transactional>>{

    private final Map<Method,Transactional> configuration = new HashMap<Method,Transactional>();
    
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

    @Override
    public boolean matches(Method method) {
        // annotated method
        if (method.getAnnotation(Transactional.class) != null){
            configuration.put(method, method.getAnnotation(Transactional.class));
            return true;
            
        // annotated class
        }else if (method.getDeclaringClass().getAnnotation(Transactional.class) != null){
            configuration.put(method, method.getDeclaringClass().getAnnotation(Transactional.class));
            return true;
        }
        
        for (Class<?> iface : method.getDeclaringClass().getInterfaces()){
            // annotated iface
            if (iface.getAnnotation(Transactional.class) != null){
                configuration.put(method, iface.getAnnotation(Transactional.class));
                return true;
            }
            
            for (Method m : iface.getMethods()){
                // annotated interface method
                if (m.getName().equals(method.getName())
                  && m.getAnnotation(Transactional.class) != null
                  && equals(m.getParameterTypes(), method.getParameterTypes())){
                    configuration.put(method, m.getAnnotation(Transactional.class));
                    return true;
                }
            }
        }
        return false;
    }
    
}
