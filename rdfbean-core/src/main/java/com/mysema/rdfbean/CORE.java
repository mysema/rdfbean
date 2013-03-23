/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean;

import com.mysema.rdfbean.model.UID;

/**
 * 
 * @author sasa
 * 
 */
public final class CORE {

    public static final String NS = "http://semantics.mysema.com/core#";

    public static final UID localId = new UID(NS, "localId");

    public static final UID enumOrdinal = new UID(NS, "enumOrdinal");

    private CORE() {
    }
}
