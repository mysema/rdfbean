/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author sasa
 *
 */
public class MemoryRepository extends AbstractSailRepository {
    
    public MemoryRepository(){}

    public MemoryRepository(File dataDir, boolean sailInference) {
        super(dataDir, sailInference);
    }

    @Override
    protected NotifyingSail createSail(File dataDir, boolean sailInference) {
        MemoryStore store = dataDir != null ? new MemoryStore(dataDir) : new MemoryStore();
        if (sailInference){
            return new ExtendedRDFSInferencer(store);
        }else{
            return store;
        }
    }
    
}
