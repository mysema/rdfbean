/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.load;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import com.mysema.rdfbean.sesame.SesameRepository;

/**
 * InferencingMemoryRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
class InferencingMemoryRepository extends SesameRepository{

    @Override
    protected Repository createRepository(boolean sesameInference) {
        return new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
    }

}
