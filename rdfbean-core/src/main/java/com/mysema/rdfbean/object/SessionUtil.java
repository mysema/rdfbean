/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;

public final class SessionUtil {
    
    private SessionUtil() {}

    public static Session openSession(Class<?>... classes) {
        return openSession(new MiniRepository(), classes);
    }

    public static Session openSession(Repository repository, Class<?>... classes) {
        return openSession(repository, (Locale) null, classes);
    }
    
    public static Session openSession(Repository repository, @Nullable Locale locale, Class<?>... classes) {
        return openSession(repository, locale != null ? Arrays.asList(locale) : Collections.<Locale>emptyList(), classes);
    }

    public static Session openSession(Repository repository, Iterable<Locale> locales, Class<?>... classes) {
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl(locales);
        sessionFactory.setConfiguration(new DefaultConfiguration(classes));
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory.openSession();
    }

    public static Session openSession(Repository repository, Package... packages) {
        return openSession(repository, (Locale) null, packages);
    }
    
    public static Session openSession(Repository repository, @Nullable Locale locale, Package... packages) {
        return openSession(repository, locale != null ? Arrays.asList(locale) : Collections.<Locale>emptyList(), packages);
    }

    public static Session openSession(Repository repository, Iterable<Locale> locales, Package... packages) {
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl(locales);
        sessionFactory.setConfiguration(new DefaultConfiguration(packages));
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        return sessionFactory.openSession();
    }
    
}
