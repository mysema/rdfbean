/**
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
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
            repository = new SailRepository(new ForwardChainingRDFSInferencer(store));
        } else {
            repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
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
