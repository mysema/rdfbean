/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;

/**
 * @author sasa
 *
 */
public abstract class AbstractSesameRepository implements Repository<SesameDialect> {

    private RDFSource[] sources;
    
    private org.openrdf.repository.Repository repository;

    public AbstractSesameRepository() {}
    
    public AbstractSesameRepository(org.openrdf.repository.Repository repository) {
        this.repository = repository;
    }

    @Override
    public RDFConnection openConnection() {
        try {
            return new SesameConnection(repository.getConnection());
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    public org.openrdf.repository.Repository getSesameRepository() {
        return repository;
    }
    
    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }

    public void initialize() throws StoreException, RDFParseException, IOException {
        repository = createRepository();
        repository.initialize();
        RepositoryConnection connection = repository.getConnection();
        try {
            if (sources != null && connection.isEmpty()) {
                ValueFactory vf = connection.getValueFactory();
                for (RDFSource source : sources) {
                    connection.add(source.openStream(), 
                            source.getContext(),
                            source.getFormat(), 
                            vf.createURI(source.getContext()));
                }
            }
        } finally {
            connection.close();
        }
    }
    
    public abstract org.openrdf.repository.Repository createRepository();

}
