/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;

import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 * 
 */
@SuppressWarnings("all")
public class PredicateImpl implements Predicate {

    private final boolean ignoreInvalid, includeInferred, inv;

    private final String context, ns, ln;

    public PredicateImpl(String context, UID uid, boolean inv) {
        this(context, uid.ns(), uid.ln(), inv);
    }

    public PredicateImpl(String context, String ns, String ln, boolean inv) {
        this.context = context;
        this.ns = ns;
        this.ln = ln;
        this.ignoreInvalid = false;
        this.includeInferred = false;
        this.inv = inv;
    }

    @Override
    public String context() {
        return context;
    }

    @Override
    public boolean ignoreInvalid() {
        return ignoreInvalid;
    }

    @Override
    public boolean includeInferred() {
        return includeInferred;
    }

    @Override
    public boolean inv() {
        return inv;
    }

    @Override
    public String ln() {
        return ln;
    }

    @Override
    public String ns() {
        return ns;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Predicate.class;
    }

}
