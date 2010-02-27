/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

/**
 * Internal interface with three implementations: BID, UID and LID.
 * 
 * @author sasa
 */
public interface Identifier {

    /**
     * @return
     */
    String getId();
    
}
