/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

/**
 * @author tiwe
 */
public class MemoryIdSequence implements IdSequence {

    private long nextId = 1;

    @Override
    public long getNextId() {
        return nextId++;
    }

}
