/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
@Immutable
public class URIMapping {

    private final UID uid;

    public URIMapping(String namespace, String localName) {
        this(null, namespace, localName, null);
    }

    public URIMapping(@Nullable String parentNs, String ns, String ln, @Nullable String elementName) {
        uid = UID.create(parentNs, ns, ln, elementName);
    }

    public String getReadableURI() {
        return Namespaces.getReadableURI(uid.ns(), uid.ln());
    }

    public UID getUID() {
        return uid;
    }
    
    public String toString() {
        return getReadableURI();
    }

}
