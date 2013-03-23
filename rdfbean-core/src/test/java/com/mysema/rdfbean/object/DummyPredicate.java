/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;

import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

@SuppressWarnings("all")
public class DummyPredicate implements Predicate {

    private final UID uid;

    public DummyPredicate(UID uid) {
        this.uid = uid;
    }

    @Override
    public String context() {
        return "";
    }

    @Override
    public boolean ignoreInvalid() {
        return false;
    }

    @Override
    public boolean includeInferred() {
        return false;
    }

    @Override
    public boolean inv() {
        return false;
    }

    @Override
    public String ln() {
        return uid.ln();
    }

    @Override
    public String ns() {
        return uid.ns();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Predicate.class;
    }

}
