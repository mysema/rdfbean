/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import javax.annotation.Nullable;

import org.openrdf.repository.Repository;

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


    public HTTPRepository(String url) {
        this.url = url;
    }


    @Override
    protected Repository createRepository(boolean sesameInference) {
        if (this.url != null) {
            return new org.openrdf.repository.http.HTTPRepository(url);
        } else {
            throw new RepositoryException("URL for remote repository not provided.");
        }
    }


    @Override
    public long getNextLocalId() {
        throw new UnsupportedOperationException("NextLocalId is not supported by remote repositories");
    }

}
