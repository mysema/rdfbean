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
import org.openrdf.sail.nativerdf.NativeStore;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.FileIdSource;
import com.mysema.rdfbean.model.IdSource;
import com.mysema.rdfbean.model.Ontology;


/**
 * Implementation of the Repository interface using NativeStore
 * 
 * @author sasa
 *
 */
public class NativeRepository extends SesameRepository {
    
    @Nullable
    private File dataDir;
        
    private IdSource idSource;
    
    public NativeRepository(){}

    public NativeRepository(File dataDir, boolean sesameInference) {
        this.dataDir = dataDir;
        setSesameInference(sesameInference);
    }
    
    public NativeRepository(File dataDir, Ontology ontology) {
        this.dataDir = dataDir;
        setOntology(ontology);
    }
    
    public NativeRepository(Ontology ontology) {
        setOntology(ontology);
    }

    @Override
    protected Repository createRepository(boolean sesameInference) {
        NativeStore store = new NativeStore(Assert.notNull(dataDir));
        idSource = new FileIdSource(new File(dataDir, "lastLocalId"));
        if (sesameInference){
            return new SailRepository(new ExtendedRDFSInferencer(store));
        }else{
            return new SailRepository(store);
        }
    }            

    @Override
    public long getNextLocalId(){
        return idSource.getNextId();
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
