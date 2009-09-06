/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.net.MalformedURLException;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.object.ConverterRegistryImpl;
import com.mysema.rdfbean.sesame.query.Functions;
import com.mysema.rdfbean.sesame.query.Operations;

/**
 * @author sasa
 *
 */
public abstract class AbstractSesameRepository implements Repository{

    private RDFSource[] sources;
    
    private org.openrdf.repository.Repository repository;
    
    private Operations operations = new Operations(new Functions(new ConverterRegistryImpl()));

    private boolean initialized = false;
    
    public AbstractSesameRepository() {}
    
    public AbstractSesameRepository(org.openrdf.repository.Repository repository) {
        this.repository = repository;
    }

    @Override
    public RDFConnection openConnection() {
        try {
            return new SesameConnection(repository.getConnection(),operations);
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

    public void initialize() {
        if (!initialized) {
            try {
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
            } catch (RDFParseException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (StoreException e) {
                throw new RuntimeException(e);
            }
            initialized = true;
        }
    }
    
    protected abstract org.openrdf.repository.Repository createRepository();

    @Override
    public void close() {
        try {
            repository.shutDown();
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }

}
