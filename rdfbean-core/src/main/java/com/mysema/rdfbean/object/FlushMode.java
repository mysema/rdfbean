/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

/**
 * FlushMode defines the strategy of how and when saved (see {@link Session#save(Object)})
 * objects are actually persisted.
 * 
 * @author sasa
 */
public enum FlushMode {
    /**
     * Changes are persisted after each {@link Session#save(Object)} or 
     * {@link Session#saveAll(Object...)} call.
     */
    ALWAYS, 
    /**
     * Changes are persisted only when {@link Session#flush()} is called.
     */
    MANUAL,
    /**
     * Changes are persisted automatically just before committing a transaction. 
     */
    COMMIT
}
