/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

/**
 * MemoryIdSource provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MemoryIdSource implements IdSource{

    private long nextId = 1;
    
    @Override
    public long getNextId() {
        return nextId++;
    }

}
