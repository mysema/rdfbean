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

import org.springframework.transaction.annotation.NotTransactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.inject.Provider;
import com.google.inject.matcher.AbstractMatcher;

/**
 * @author tiwe
 */
class TransactionalMethodMatcher extends AbstractMatcher<Method> implements Provider<Map<Method, Transactional>> {

    private final Map<Method, Transactional> configuration = new HashMap<Method, Transactional>();

    @Override
    public boolean matches(Method method) {
        if (method.getAnnotation(NotTransactional.class) != null) {
            return false;

            // annotated method
        } else if (method.getAnnotation(Transactional.class) != null) {
            return handle(method, method.getAnnotation(Transactional.class));

            // annotated class
        } else if (method.getDeclaringClass().getAnnotation(Transactional.class) != null) {
            return handle(method, method.getDeclaringClass().getAnnotation(Transactional.class));
        }

        for (Class<?> iface : method.getDeclaringClass().getInterfaces()) {
            // annotated interface
            if (iface.getAnnotation(Transactional.class) != null) {
                return handle(method, iface.getAnnotation(Transactional.class));
            } else {
                for (Method m : iface.getMethods()) {
                    // annotated interface method
                    if (m.getName().equals(method.getName())
                            && m.getAnnotation(Transactional.class) != null
                            && equals(m.getParameterTypes(), method.getParameterTypes())) {
                        return handle(method, m.getAnnotation(Transactional.class));
                    }
                }
            }
        }
        return false;
    }

    private boolean handle(Method method, Transactional annotation) {
        boolean intercepted = annotation.propagation() != Propagation.SUPPORTS;
        if (intercepted) {
            configuration.put(method, annotation);
        }
        return intercepted;
    }

    private boolean equals(Object[] a, Object[] b) {
        if (a.length == b.length) {
            return Arrays.asList(a).equals(Arrays.asList(b));
        }
        return false;
    }

    @Override
    public Map<Method, Transactional> get() {
        return configuration;
    }

}
