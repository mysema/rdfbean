/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.mulgara;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Triple;
import org.mulgara.client.jrdf.GraphElementBuilder;
import org.mulgara.connection.Connection;
import org.mulgara.query.QueryException;
import org.mulgara.query.operation.Deletion;
import org.mulgara.query.operation.Insertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SimpleBeanQuery;

/**
 * MulgaraConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MulgaraConnection implements RDFConnection{
    
    private static final Logger logger = LoggerFactory.getLogger(MulgaraConnection.class);
    
    private final Connection connection;
    
    private final GraphElementFactory elementFactory;
    
    private final MulgaraDialect dialect;

    private MulgaraTransaction localTxn;

    private boolean readonlyTnx = false;
    
    public MulgaraConnection(Connection connection) {        
        try {
            this.connection = Assert.notNull(connection);
            this.elementFactory = new GraphElementBuilder();
            this.dialect = new MulgaraDialect(elementFactory);
        } catch (GraphException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    public void cleanUpAfterCommit(){
        localTxn = null;
        readonlyTnx = false;
    }
    
    public void cleanUpAfterRollback(){
        localTxn = null;
        readonlyTnx = false;
    }
    
    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout,
            int isolationLevel) {
        localTxn = new MulgaraTransaction(this, connection, readOnly, txTimeout, isolationLevel);
        readonlyTnx = readOnly;
        return localTxn;
    }

    @Override
    public void clear() {
    }

    @Override
    public BID createBNode() {
        return new BID();
    }

    @Override
    public BeanQuery createQuery(Session session) {
        return new SimpleBeanQuery(session);
    }

    @Override
    public <Q> Q createQuery(Session session, Class<Q> queryType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {
        if (!readonlyTnx){
            try {
                // TODO : what about contexts ?!?
                if (!removedStatements.isEmpty()){
                    connection.execute(new Deletion(null, convert(removedStatements)));
                }
                if (!addedStatements.isEmpty()){
                    connection.execute(new Insertion(null, convert(addedStatements)));
                }
            } catch (Exception e) {
                String error = "Caught " + e.getClass().getName();
                logger.error(error, e);
                throw new RuntimeException(error, e);
            }
        }
    }
    
    private Set<Triple> convert(Collection<STMT> statements){
        Set<Triple> triples = new HashSet<Triple>(statements.size());
        for (STMT stmt : statements){
            triples.add(dialect.createStatement(
                    stmt.getSubject(), 
                    stmt.getPredicate(),
                    stmt.getObject(),
                    stmt.getContext()));
        }
        return triples;
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (QueryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        
    }

}
