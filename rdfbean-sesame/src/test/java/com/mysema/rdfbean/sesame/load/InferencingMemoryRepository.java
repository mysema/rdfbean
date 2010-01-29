/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.load;

import java.io.File;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import com.mysema.rdfbean.sesame.AbstractSailRepository;

/**
 * InferencingMemoryRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
class InferencingMemoryRepository extends AbstractSailRepository{

    @Override
    protected NotifyingSail createSail(File dataDir, boolean sailInference) {
        return new ForwardChainingRDFSInferencer(new MemoryStore());
    }

}
