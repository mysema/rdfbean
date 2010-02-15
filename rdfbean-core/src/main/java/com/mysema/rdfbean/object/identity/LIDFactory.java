/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.Serializable;

import org.apache.commons.collections15.Factory;

import com.mysema.rdfbean.model.LID;

/**
 * 
 * LIDFactory provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public final class LIDFactory implements Factory<LID>, Serializable {

    private static final long serialVersionUID = 7709602558416583665L;
    
    private int nextId;

    public LIDFactory() {
        this(1);
    }

    public LIDFactory(int nextId) {
        this.nextId = nextId;
    }

    @Override
    public LID create() {
        return new LID(nextId++);
    }
    
    public int getNextId() {
        return nextId;
    }

}