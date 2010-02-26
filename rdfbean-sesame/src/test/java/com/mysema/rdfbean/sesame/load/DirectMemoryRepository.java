/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.load;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.mysema.rdfbean.sesame.SesameRepository;

/**
 * DirectMemoryRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
class DirectMemoryRepository extends SesameRepository{

    private long nextLocalId = 1;
    
    @Override
    protected Repository createRepository(boolean sesameInference) {
        return new SailRepository(new MemoryStore());
    }

    @Override
    public long getNextLocalId() {
        return nextLocalId++;
    }

}
