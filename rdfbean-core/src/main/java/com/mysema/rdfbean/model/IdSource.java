/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

/**
 * IdSource provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface IdSource {

    /**
     * @return
     */
    long getNextId();

}