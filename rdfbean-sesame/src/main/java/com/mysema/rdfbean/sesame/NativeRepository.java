/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.nativerdf.NativeStore;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.Ontology;


/**
 * Implementation of the Repository interface using NativeStore
 * 
 * @author sasa
 *
 */
public class NativeRepository extends AbstractSailRepository {
    
    public NativeRepository(){}

    public NativeRepository(File dataDir, boolean sailInference) {
        super(dataDir, sailInference);
    }
    
    public NativeRepository(File dataDir, Ontology ontology) {
        super(dataDir, ontology);
    }
    
    public NativeRepository(Ontology ontology) {
        super(ontology);
    }

    @Override
    protected NotifyingSail createSail(File dataDir, boolean sailInference) {
        NativeStore store = new NativeStore(Assert.notNull(dataDir));
        if (sailInference){
            return new ExtendedRDFSInferencer(store);
        }else{
            return store;
        }
    }
    
}
