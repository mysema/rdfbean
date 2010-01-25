/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author sasa
 *
 */
public class MemoryRepository extends AbstractSesameRepository {
    
    private File dataDir;

    @Override
    public Repository createRepository() {
        Repository repository;
        if (dataDir != null) {
            MemoryStore store = new MemoryStore(dataDir);
            repository = new SailRepository(new ExtendedRDFSInferencer(store));
        } else {
            repository = new SailRepository(new ExtendedRDFSInferencer(new MemoryStore()));
        }
        return repository;
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
