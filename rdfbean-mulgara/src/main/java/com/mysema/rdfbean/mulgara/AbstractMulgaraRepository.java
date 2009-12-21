/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.mulgara;

import org.mulgara.connection.Connection;
import org.mulgara.connection.ConnectionException;
import org.mulgara.connection.ConnectionFactory;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;

/**
 * AbstractMulgaraRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractMulgaraRepository implements Repository{

    private static final Logger logger = LoggerFactory.getLogger(AbstractMulgaraRepository.class);
    
    private boolean initialized;
    
    private Database database;
    
    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    
    @Override
    public void close() {
        database.close();        
    }

    @Override
    public void initialize() {
        if (!initialized){
            database = createDatabase();
            initialized = true;
        }        
    }

    protected abstract Database createDatabase();

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

}
