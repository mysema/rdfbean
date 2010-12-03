/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;

import javax.annotation.Nullable;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.Inference;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;
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

    private static final Logger logger = LoggerFactory.getLogger(SesameRepository.class);

    @Nullable
    private Ontology<UID> ontology;

    private RDFSource[] sources;

    private org.openrdf.repository.Repository repository;

    private boolean initialized = false;

    private boolean sesameInference = false;

    private Inference inference = Inference.FULL;

    @Override
    public void close() {
        try {
            initialized = false;
            repository.shutDown();
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    protected abstract org.openrdf.repository.Repository createRepository(boolean sesameInference);

    @Override
    public <RT> RT execute(Operation<RT> operation) {
        RDFConnection connection = openConnection();
        try{
            RDFBeanTransaction tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
            try{
                RT retVal = operation.execute(connection);
                tx.commit();
                return retVal;
            }catch(IOException io){
                tx.rollback();
                throw new RepositoryException(io);
            }
        }finally{
            connection.close();
        }
    }

    @Override
    public void export(Format format, OutputStream out) {
        export(format, Namespaces.DEFAULT, out);
    }

    @Override
    public void export(Format format, Map<String, String> ns2prefix, OutputStream out){
        RDFFormat targetFormat = FormatHelper.getFormat(format);
        RDFWriter writer = Rio.createWriter(targetFormat, out);
        try {
            RepositoryConnection conn = repository.getConnection();
            for(Map.Entry<String, String> entry : ns2prefix.entrySet()){
                conn.setNamespace(entry.getValue(), entry.getKey());
            }
            try{
                conn.export(writer);
            }finally{
                conn.close();
            }
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (RDFHandlerException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    protected Inference getInferenceOptions() {
        return inference;
    }

    public abstract long getNextLocalId();

    public org.openrdf.repository.Repository getSesameRepository() {
        return repository;
    }

    public void initialize() {
        if (!initialized) {
            try {
                repository = createRepository(sesameInference);
                repository.initialize();
                RepositoryConnection connection = repository.getConnection();
                connection.setAutoCommit(false);
                try {
                    if (sources != null && connection.isEmpty()) {
                        ValueFactory vf = connection.getValueFactory();
                        for (RDFSource source : sources) {
                            if (source.getResource() != null){
                                logger.info("loading " + source.getResource());
                            }
                            connection.add(source.openStream(),
                                    source.getContext(),
                                    FormatHelper.getFormat(source.getFormat()),
                                    vf.createURI(source.getContext()));
                        }
                    }
                    connection.commit();
                } catch(Exception e){
                    connection.rollback();
                    throw new RepositoryException(e);
                } finally {
                    connection.close();
                }

                if (ontology == null){
                    ontology = EmptyOntology.DEFAULT;
                    RepositoryOntology schemaOntology = new RepositoryOntology(this);
                    ontology = schemaOntology;
                }

            } catch (MalformedURLException e) {
                throw new RepositoryException(e);
            } catch (IOException e) {
                throw new RepositoryException(e);
            } catch (org.openrdf.repository.RepositoryException e) {
                throw new RepositoryException(e);
            }
            initialized = true;
        }
    }

    @Override
    public void load(Format format, InputStream is, @Nullable UID context, boolean replace){
        try {
            RepositoryConnection connection = repository.getConnection();
            ValueFactory vf = connection.getValueFactory();
            try{
                URI contextURI = context != null ? vf.createURI(context.getId()) : null;
                if (!replace && context != null){
                    if (connection.hasStatement(null, null, null, true, contextURI)){
                        return;
                    }
                }
                if (context != null && replace){
                    connection.remove((Resource)null, null, null, contextURI);
                }
                if (context == null){
                    connection.add(is, TEST.NS, FormatHelper.getFormat(format));
                }else{
                    connection.add(is, context.getId(), FormatHelper.getFormat(format), contextURI);
                }

            }finally {
                connection.close();
            }
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        } catch (RDFParseException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public RDFConnection openConnection() {
        try {
            return new SesameConnection(this, repository.getConnection(), ontology, getInferenceOptions());
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    public final void setOntology(Ontology<UID> ontology) {
        this.ontology = ontology;
    }

    public final void setSesameInference(boolean sesameInference) {
        this.sesameInference = sesameInference;
        this.inference = sesameInference ? Inference.LITERAL : Inference.FULL;
    }

    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }
}
