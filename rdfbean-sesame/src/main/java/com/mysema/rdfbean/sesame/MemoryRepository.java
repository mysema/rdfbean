/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.memory.MemoryStore;

import com.mysema.rdfbean.model.Ontology;

/**
 * Implementation of the Repository interface using MemoryStore
 * 
 * @author sasa
 *
 */
public class MemoryRepository extends AbstractSailRepository {
    
    public MemoryRepository(){}

    public MemoryRepository(File dataDir, boolean sesameInference) {
        super(dataDir, sesameInference);
    }

    public MemoryRepository(File dataDir, Ontology ontology) {
        super(dataDir, ontology);
    }
    
    public MemoryRepository(Ontology ontology) {
        super(ontology);
    }

    @Override
    protected NotifyingSail createSail(File dataDir, boolean sesameInference) {
        MemoryStore store = dataDir != null ? new MemoryStore(dataDir) : new MemoryStore();
        if (sesameInference){
            return new ExtendedRDFSInferencer(store);
        }else{
            return store;
        }
    }

}
