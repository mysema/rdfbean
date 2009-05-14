/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author sasa
 *
 */
public class MemorySailRepositoryFactoryBean implements FactoryBean {

	private List<RDFSource> sources;
	
	private File dataDir;
	
	private static Map<String, Repository> repositories = new HashMap<String, Repository>();
	
	public void setSources(List<RDFSource> sources) {
		this.sources = sources;
	}

	@Override
	public Object getObject() throws Exception {
	    Repository repository;
	    if (dataDir != null) {
	        repository = repositories.get(dataDir.getAbsoluteFile().getName());
    	    if (repository == null) {
        	    MemoryStore store = new MemoryStore(dataDir);
                repository = new SailRepository(new ForwardChainingRDFSInferencer(store));
                initialize(repository);
                repositories.put(dataDir.getAbsoluteFile().getName(), repository);
    	    }
        } else {
            repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
            initialize(repository);
        }
		return repository;
	}
	
	private void initialize(Repository repository) throws RepositoryException, RDFParseException, IOException {
        repository.initialize();
        RepositoryConnection connection = repository.getConnection();
        try {
            if (sources != null && connection.isEmpty()) {
            	ValueFactory vf = connection.getValueFactory();
            	for (RDFSource source : sources) {
            		connection.add(source.getResource().getInputStream(), 
            				source.getContext(),
            				source.getFormat(), 
            				vf.createURI(source.getContext()));
            	}
            }
        } finally {
            connection.close();
        }
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getObjectType() {
		return Repository.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
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
