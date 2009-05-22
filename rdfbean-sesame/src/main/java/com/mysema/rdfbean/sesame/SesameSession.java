/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.Closeable;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.ModelResult;
import org.openrdf.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.types.expr.EEntity;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.object.*;
import com.mysema.rdfbean.sesame.query.SesameQuery;

/**
 * @author sasa
 * 
 */
public class SesameSession extends AbstractSession<Value, Resource, BNode, URI, Literal, Statement> implements Closeable {
    
    private static final Logger logger = LoggerFactory.getLogger(SesameSession.class);
    
    private RepositoryConnection connection;
    
    private SesameDialect dialect;
    
    private SesameTransaction localTxn = null;
    
    private boolean roTnx = false;

    private ValueFactory vf;
    
    public SesameSession(RepositoryConnection connection, Class<?>... classes) {
        this(connection, Collections.<Locale>emptyList(), new DefaultConfiguration(classes));
    }

    public SesameSession(RepositoryConnection connection, Configuration ctx) {
        this(connection, Collections.<Locale>emptyList(), ctx);
    }
    
    public SesameSession(RepositoryConnection connection, List<Locale> locales, Class<?>... classes) {
        this(connection, locales, new DefaultConfiguration(classes));
    }
    
    public SesameSession(RepositoryConnection connection, List<Locale> locales, Configuration ctx) {
        super(locales, ctx);       
        this.connection = connection;
        this.vf = connection.getValueFactory();
        this.dialect = new SesameDialect(vf);
    }

    public SesameSession(RepositoryConnection connection, List<Locale> locales, Package... packages) throws ClassNotFoundException {
        this(connection, locales, new DefaultConfiguration(packages));
    }
    
    public SesameSession(RepositoryConnection connection, Locale locale, Class<?>... classes) {
        this(connection, Collections.singletonList(locale), new DefaultConfiguration(classes));
    }
    
    public SesameSession(RepositoryConnection connection, Locale locale, Configuration ctx) {
        this(connection, Collections.singletonList(locale), ctx);
    }

    public SesameSession(RepositoryConnection connection, Locale locale, Package... packages) throws ClassNotFoundException {
        this(connection, Collections.singletonList(locale), new DefaultConfiguration(packages));
    }
    
    public SesameSession(RepositoryConnection connection, Package... packages) {
        this(connection, Collections.<Locale>emptyList(), new DefaultConfiguration(packages));
    }

    @Override
    protected void addStatement(Statement statement, URI context) {
        if (!roTnx){
            try {
                if (context == null) {
                    connection.add(statement);
                } else {
                    connection.add(statement, context);
                }
            } catch (StoreException e) {
                throw new RuntimeException(e);
            }    
        }        
    }

    public RDFBeanTransaction beginTransaction() {
      return beginTransaction(false, -1, Connection.TRANSACTION_READ_COMMITTED);
    }
        
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        logger.debug("beginTransaction");
        createTransaction(readOnly, txTimeout, isolationLevel);
        localTxn.begin();
        return localTxn;
    }

    public void cleanUpAfterCommit(){
        localTxn = null;
        roTnx = false;
        clear();
    }
    
    public void cleanUpAfterRollback(){
        localTxn = null;
        roTnx = false;
        close();
    }
    
    @Override
    public void close() {
        clear();
        try {
            if (localTxn != null) {
                localTxn.rollback();
            }
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
    private RDFBeanTransaction createTransaction(boolean readOnly, int txTimeout, int isolationLevel){
        localTxn = new SesameTransaction(this, isolationLevel);        
        roTnx = readOnly;
        return localTxn;
    }

    @Override
    protected List<Statement> findStatements(Resource subject, URI predicate, Value object, boolean includeInferred, URI context) {
        ModelResult statements = null;
        try {
            if (context == null) {
                statements = connection.match(subject, predicate, object, includeInferred);
            } else if (includeInferred) {
                statements = connection.match(subject, predicate, object, includeInferred, context, null);
            } else {
                statements = connection.match(subject, predicate, object, includeInferred, context);
            }
            return statements.asList();
        } catch (StoreException e) {
            throw new RuntimeException(e);
        } finally {
            if (statements != null) {
                try {
                    statements.close();
                } catch (StoreException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }
    
    @Override
    public BeanQuery from(EEntity<?>... exprs) {
        SesameQuery query = new SesameQuery(this);
        query.getMetadata().setDistinct(true);
        return new BeanQueryAdapter(query,query).from(exprs);
    }
    
    public RepositoryConnection getConnection(){
        return connection;
    }
    
    @Override
    public Dialect<Value, Resource, BNode, URI, Literal, Statement> getDialect() {
        return dialect;
    }
    
    public RDFBeanTransaction getTransaction() {
        return localTxn;
    }
    
    public boolean isReadOnly(){
        return roTnx;
    }
    
    @Override
    protected void removeStatement(Statement statement, URI context) {
        if (!roTnx){
            try {
                if (context == null) {
                    connection.remove(statement, context);
                } else {
                    connection.remove(statement);
                }
            } catch (StoreException e) {
                throw new RuntimeException(e);
            }    
        }
    }
   
}
