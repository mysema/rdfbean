/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.sql.Connection;

import javax.annotation.Nullable;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.model.Inference;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.ontology.EmptyOntology;
import com.mysema.rdfbean.ontology.Ontology;
import com.mysema.rdfbean.ontology.RepositoryOntology;

/**
 * SesameRepository provides a base class for Sesame repository based RDFBean repositories 
 * 
 * @author sasa
 * @author tiwe
 *
 */
public abstract class SesameRepository implements Repository{
    
    @Nullable
    private Ontology ontology;
    
    private RDFSource[] sources;
    
    private org.openrdf.repository.Repository repository;

    private boolean initialized = false;
    
    private boolean sesameInference = false;
    
    private Inference inference = Inference.FULL;
        
    public SesameRepository() {}
    
    public SesameRepository(org.openrdf.repository.Repository repository) {
        this.repository = repository;
    }

    @Override
    public RDFConnection openConnection() {
        try {
            return new SesameConnection(this, repository.getConnection(), ontology, getInferenceOptions());
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }
    
    public org.openrdf.repository.Repository getSesameRepository() {
        return repository;
    }
    
    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }
    
    public abstract long getNextLocalId();
    
    public void initialize() {
        if (!initialized) {
            try {
                repository = createRepository(sesameInference);
                repository.initialize();
                RepositoryConnection connection = repository.getConnection();
                try {
                    if (sources != null && connection.isEmpty()) {
                        ValueFactory vf = connection.getValueFactory();
                        for (RDFSource source : sources) {
                            connection.add(source.openStream(), 
                                    source.getContext(),
                                    FormatHelper.getFormat(source.getFormat()), 
                                    vf.createURI(source.getContext()));
                        }
                    }
                } finally {
                    connection.close();
                }
                
                if (ontology == null){
                    ontology = EmptyOntology.DEFAULT;
                    RepositoryOntology schemaOntology = new RepositoryOntology(this);
                    ontology = schemaOntology;
                }
                
            } catch (RDFParseException e) {
                throw new RepositoryException(e);
            } catch (MalformedURLException e) {
                throw new RepositoryException(e);
            } catch (IOException e) {
                throw new RepositoryException(e);
            } catch (StoreException e) {
                throw new RepositoryException(e);
            }
            initialized = true;
        }
    }
    
    protected abstract org.openrdf.repository.Repository createRepository(boolean sesameInference);
    
    @Override
    public void export(Format format, OutputStream out){
        RDFFormat targetFormat = FormatHelper.getFormat(format);
        RDFWriter writer = Rio.createWriter(targetFormat, out);
        try {
            RepositoryConnection conn = repository.getConnection();
            try{
                conn.export(writer);    
            }finally{
                conn.close();
            }
        } catch (StoreException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (RDFHandlerException e) {
            throw new RepositoryException(e.getMessage(), e);
        }                     
    }
    
    @Override
    public void close() {
        try {
            initialized = false;
            repository.shutDown();
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }
    
    @Override
    public void execute(Operation operation) {
        RDFConnection connection = openConnection();
        try{
            try{
                RDFBeanTransaction tx = connection.beginTransaction(false, 0, Connection.TRANSACTION_READ_COMMITTED);
                try{
                    operation.execute(connection);    
                    tx.commit();
                }catch(IOException io){
                    tx.rollback();
                }                
            }finally{
                connection.close();
            }    
        }catch(IOException io){
            throw new RepositoryException(io);
        }
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }
    
    protected Inference getInferenceOptions() {
        return inference;
    }

    public void setSesameInference(boolean sesameInference) {
        this.sesameInference = sesameInference;
        this.inference = sesameInference ? Inference.LITERAL : Inference.FULL;
    }
}
