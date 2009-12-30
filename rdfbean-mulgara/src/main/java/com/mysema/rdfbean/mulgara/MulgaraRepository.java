/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.mulgara;

import java.io.File;
import java.net.URI;

import javax.activation.MimeType;

import org.mulgara.config.MulgaraConfig;
import org.mulgara.connection.Connection;
import org.mulgara.connection.ConnectionException;
import org.mulgara.connection.ConnectionFactory;
import org.mulgara.query.QueryException;
import org.mulgara.query.operation.Load;
import org.mulgara.resolver.Database;
import org.mulgara.resolver.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.io.RDFSource;

/**
 * AbstractMulgaraRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MulgaraRepository implements Repository{

    private static final Logger logger = LoggerFactory.getLogger(MulgaraRepository.class);
    
    private boolean initialized;
    
    private final URI uri;
    
    private final File directory;
    
    private final MulgaraConfig config;
    
    private Database database;
    
    private RDFSource[] sources;
    
    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    
    public MulgaraRepository(URI uri, File directory, MulgaraConfig config){
        this.uri = Assert.notNull(uri, "uri was null");
        this.directory = Assert.notNull(directory, "directory was null");
        this.config = Assert.notNull(config, "config was null");
    }
    
    @Override
    public void close() {
        if (database != null){
            database.close();        
        }
    }

    @Override
    public void initialize() {
        if (!initialized){
            database = createDatabase(uri, directory, config);
            if (sources != null){
                try {
                    Connection connection = connectionFactory.newConnection(database.newSession());
                    try {
                        for (RDFSource source : sources){
                            MimeType contentType = new MimeType(source.getFormat().getMimetype());
                            Load load = new Load(URI.create(source.getContext()), source.openStream(), contentType);
                            connection.execute(load);    
                        }                    
                    } finally {
                        connection.close();
                    }    
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                
            }            
            initialized = true;            
        }        
    }

    protected Database createDatabase(URI uri, File directory, MulgaraConfig config){
        try {
            return DatabaseFactory.newDatabase(uri, directory, config);
        } catch (Exception e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    @Override
    public RDFConnection openConnection() {
        try {
            Connection connection = connectionFactory.newConnection(database.newSession());
            return new MulgaraConnection(connection);
        } catch (ConnectionException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        } catch (QueryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }        
    }

    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }
    
    

}
