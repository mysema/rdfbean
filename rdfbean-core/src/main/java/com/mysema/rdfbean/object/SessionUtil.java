/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Arrays;
import java.util.Locale;

import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;

public final class SessionUtil {
    
    private SessionUtil() {}

    public static Session openSession(Class<?>... classes) {
        return openSession(new MiniRepository(), classes);
    }

    public static Session openSession(Repository<?> repository, Class<?>... classes) {
        return openSession(repository, (Locale) null, classes);
    }

    public static Session openSession(RDFConnection connection, Package... packages) {
        return openSession(connection, (Locale) null, packages);
    }
    
    public static Session openSession(Repository<?> repository, Locale locale, Class<?>... classes) {
        return openSession(repository, locale != null ? Arrays.asList(locale) : null, classes);
    }

    public static Session openSession(Repository<?> repository, Iterable<Locale> locales, Class<?>... classes) {
        return new SessionImpl(new DefaultConfiguration(classes), repository.openConnection(), locales);
    }
    
    public static Session openSession(RDFConnection connection, Class<?>... classes) {
        return openSession(connection, (Locale) null, classes);
    }
    
    public static Session openSession(RDFConnection connection, Locale locale, Class<?>... classes) {
        return openSession(connection, locale != null ? Arrays.asList(locale) : null, classes);
    }

    public static Session openSession(RDFConnection connection, Iterable<Locale> locales, Class<?>... classes) {
        return new SessionImpl(new DefaultConfiguration(classes), connection, 
                locales);
    }

    public static Session openSession(RDFConnection connection, Locale locale, Package... packages) {
        return openSession(connection, locale != null ? Arrays.asList(locale) : null, packages);
    }

    public static Session openSession(RDFConnection connection, Iterable<Locale> locales, Package... packages) {
        return new SessionImpl(new DefaultConfiguration(packages), connection, locales);
    }

}
