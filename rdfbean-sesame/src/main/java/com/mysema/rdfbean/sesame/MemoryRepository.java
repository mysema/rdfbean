/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.mysema.rdfbean.model.FileIdSource;
import com.mysema.rdfbean.model.Ontology;

/**
 * Implementation of the Repository interface using MemoryStore
 * 
 * @author sasa
 *
 */
public class MemoryRepository extends SesameRepository {
    
    @Nullable
    private File dataDir;
    
    @Nullable
    private FileIdSource idSource;
        
    public MemoryRepository(){}

    public MemoryRepository(File dataDir, boolean sesameInference) {
        this.dataDir = dataDir;
        setSesameInference(sesameInference);
    }

    public MemoryRepository(File dataDir, Ontology ontology) {
        this.dataDir = dataDir;
        setOntology(ontology);
    }
    
    public MemoryRepository(Ontology ontology) {
        setOntology(ontology);
    }
    
    @Override
    protected Repository createRepository(boolean sesameInference) {
        MemoryStore store;
        if (dataDir != null){
            store = new MemoryStore(dataDir);
            idSource = new FileIdSource(new File(dataDir, "lastLocalId"));
        }else{
            store = new MemoryStore();
        }
        if (sesameInference){
            return new SailRepository(new ExtendedRDFSInferencer(store));
        }else{
            return new SailRepository(store);
        }
    }    
    
    @Override
    public long getNextLocalId(){
        return idSource != null ? idSource.getNextId() : super.getNextLocalId();
    }
   
    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }
    
    public void setDataDirName(String dataDirName) {
        if (StringUtils.isNotEmpty(dataDirName)) {
            this.dataDir = new File(dataDirName);
        }
    }

}
