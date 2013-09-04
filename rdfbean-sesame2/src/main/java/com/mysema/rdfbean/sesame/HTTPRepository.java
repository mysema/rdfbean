/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.File;

import javax.annotation.Nullable;

import org.openrdf.repository.Repository;

import com.mysema.rdfbean.model.IdSequence;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * Implementation of the Repository interface using HTTPRepository
 * 
 * @author marek-surek
 * 
 */
public class HTTPRepository extends SesameRepository {

    @Nullable
    private String url;

    @Nullable
    private File dataDir;
    
    @Nullable
    private IdSequence idSource;
    
    @Nullable
    private String username, password;

    public HTTPRepository(String url) {
        this.url = url;
        setSerializeQueries(true);
    }

    @Override
    protected Repository createRepository(boolean sesameInference) {
        if (this.url != null) {
            org.openrdf.repository.http.HTTPRepository repository = new org.openrdf.repository.http.HTTPRepository(url);
            if (dataDir != null) {
                repository.setDataDir(dataDir);
            }
            if (username != null || password != null) {
                repository.setUsernameAndPassword(username, password);
            }
            idSource = new MemoryIdSequence();
            return repository;
        } else {
            throw new RepositoryException("URL for remote repository not provided.");
        }
    }

    @Override
    public long getNextLocalId() {
        return idSource.getNextId();
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public void setUsernameAndPassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
