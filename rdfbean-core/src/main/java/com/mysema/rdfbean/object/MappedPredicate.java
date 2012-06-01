/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
@Immutable
public final class MappedPredicate {

    private final UID uid;

    private final boolean inv;

    private final boolean ignoreInvalid;

    private final boolean includeInferred;

    @Nullable
    private final UID context;

    public MappedPredicate(UID uid, boolean inv) {
        this.uid = uid;
        this.inv = inv;
        this.ignoreInvalid = false;
        this.includeInferred = false;
        this.context = null;
    }

    public MappedPredicate(String parentNs, Predicate predicate, @Nullable String elementName) {
        this.uid = UID.create(parentNs, predicate.ns(), predicate.ln(), elementName);
        this.inv = predicate.inv();
        this.ignoreInvalid = predicate.ignoreInvalid();
        this.includeInferred = predicate.includeInferred();
        if (predicate.context().isEmpty()){
            this.context = null;
        }else{
            this.context = new UID(predicate.context());
        }
    }

    public boolean inv() {
        return inv;
    }

    public boolean ignoreInvalid() {
        return ignoreInvalid;
    }

    public boolean includeInferred() {
        return includeInferred;
    }

    @Nullable
    public UID getContext() {
        return context;
    }

    public UID getUID() {
        return uid;
    }

    @Override
    public String toString() {
        return uid.getId();
    }
}
