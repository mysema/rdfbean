/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import javax.annotation.Nullable;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.google.common.base.Strings;
import com.mysema.rdfbean.model.FileIdSequence;
import com.mysema.rdfbean.model.IdSequence;
import com.mysema.rdfbean.model.MemoryIdSequence;

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
    private IdSequence idSource;
    
    /**
     * the synchronization delay is specified by a number, indicating the time
     * in milliseconds that the store will wait before it synchronizes changes
     * to disk.
     */
    private long syncDelay;
        
    public MemoryRepository(){}
    
    public MemoryRepository(@Nullable File dataDir, long syncDelay, boolean sesameInference) {
        this.dataDir = dataDir;
        setSyncDelay(syncDelay);
        setSesameInference(sesameInference);
    }

    public MemoryRepository(@Nullable File dataDir, boolean sesameInference) {
        this.dataDir = dataDir;
        setSesameInference(sesameInference);
    }

    public MemoryRepository(@Nullable File dataDir) {
        this.dataDir = dataDir;
    }
    
    @Override
    protected Repository createRepository(boolean sesameInference) {
        MemoryStore store;
        if (dataDir != null){
            store = new MemoryStore(dataDir);
            idSource = new FileIdSequence(new File(dataDir, "lastLocalId"));
        }else{
            store = new MemoryStore();
            idSource = new MemoryIdSequence();
        }
        if (syncDelay > 0){
            store.setSyncDelay(syncDelay);
        }
        
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
   
    public final void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }
    
    public final void setDataDirName(String dataDirName) {
        if (!Strings.isNullOrEmpty(dataDirName)) {
            this.dataDir = new File(dataDirName);
        }
    }

    public final void setSyncDelay(long syncDelay) {
        this.syncDelay = syncDelay;
    }

    
}
