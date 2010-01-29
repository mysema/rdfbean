package com.mysema.rdfbean.sesame;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;

import com.mysema.rdfbean.model.InferenceOptions;
import com.mysema.rdfbean.model.Ontology;

/**
 * AbstractSailRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractSailRepository extends AbstractSesameRepository{
    
    @Nullable
    private File dataDir;
        
    private boolean sesameInference = false;
    
    public AbstractSailRepository(){}
    
    public AbstractSailRepository(File dataDir, boolean sailInference){
        this.dataDir = dataDir;
        this.sesameInference = sailInference;
    }
    
    public AbstractSailRepository(File dataDir, Ontology ontology) {
        this.dataDir = dataDir;
        setOntology(ontology);
    }

    public AbstractSailRepository(Ontology ontology) {
        setOntology(ontology);
    }

    protected abstract NotifyingSail createSail(@Nullable File dataDir, boolean sailInference);
    
    @Override
    public Repository createRepository() {
        return new SailRepository(createSail(dataDir, sesameInference));
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }
    
    public void setDataDirName(String dataDirName) {
        if (StringUtils.isNotEmpty(dataDirName)) {
            this.dataDir = new File(dataDirName);
        }
    }    

    @Override
    protected InferenceOptions getInferenceOptions() {
        return new InferenceOptions(!sesameInference, !sesameInference, true);
    }

    public void setSesameInference(boolean sesameInference) {
        this.sesameInference = sesameInference;
    }
    
}
