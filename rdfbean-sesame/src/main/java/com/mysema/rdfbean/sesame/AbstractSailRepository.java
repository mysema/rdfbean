package com.mysema.rdfbean.sesame;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;

/**
 * AbstractSailRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractSailRepository extends AbstractSesameRepository{
    
    @Nullable
    private File dataDir;
    
    private boolean sailInference = false;
    
    public AbstractSailRepository(){}
    
    public AbstractSailRepository(File dataDir, boolean sailInference){
        this.dataDir = dataDir;
        this.sailInference = sailInference;
    }
    
    protected abstract NotifyingSail createSail(@Nullable File dataDir, boolean sailInference);
    
    @Override
    public Repository createRepository() {
        return new SailRepository(createSail(dataDir, sailInference));
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }
    
    public void setDataDirName(String dataDirName) {
        if (StringUtils.isNotEmpty(dataDirName)) {
            this.dataDir = new File(dataDirName);
        }
    }

    public void setSailInference(boolean sailInference) {
        this.sailInference = sailInference;
    }

    
}
