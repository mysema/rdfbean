/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.spring;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import com.mysema.commons.l10n.support.LocaleIterable;
import com.mysema.rdfbean.object.SessionFactoryImpl;

/**
 * @author sasa
 *
 */
public class SpringSessionFactory extends SessionFactoryImpl {

    @Override
    public Iterable<Locale> getLocales() {
        return new LocaleIterable(LocaleContextHolder.getLocale(), true);
    }

}
