/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.iterators.TransformIterator;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.ModelResult;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.query.SesameQuery;

/**
 * @author sasa
 * 
 */
public class SesameConnection implements RDFConnection {
    
    private static final boolean datatypeInference = true;
    
    private final RepositoryConnection connection;
    
    private final SesameDialect dialect;
    
    @Nullable
    private SesameTransaction localTxn = null;

    private boolean readonlyTnx = false;
    
    private final ValueFactory vf;
    
    private final Transformer<STMT,Statement> stmtTransformer = new Transformer<STMT,Statement>(){
        @Override
        public Statement transform(STMT stmt) {
            return convert(stmt);
        }        
    };
    
    public SesameConnection(RepositoryConnection connection) {
        this.connection = connection;
        this.vf = connection.getValueFactory();
        this.dialect = new SesameDialect(vf);
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        localTxn = new SesameTransaction(this, isolationLevel);        
        readonlyTnx = readOnly;
        localTxn.begin();
        return localTxn;
    }
    
    public void cleanUpAfterCommit(){
        localTxn = null;
        readonlyTnx = false;
    }
    
    public void cleanUpAfterRollback(){
        localTxn = null;
        readonlyTnx = false;
        close();
    }

    @Override
    public void clear() {        
    }

    @Override
    public void close() {
        try {
            if (localTxn != null) {
                localTxn.rollback();
            }
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
    private Iterable<Statement> convert(final Collection<STMT> stmts) {
        return new Iterable<Statement>(){
            @Override
            public Iterator<Statement> iterator() {
                return new TransformIterator<STMT,Statement>(stmts.iterator(),stmtTransformer);
            }
            
        };
    }
    
    private Statement convert(STMT stmt){
        Resource subject = dialect.getResource(stmt.getSubject());
        URI predicate = dialect.getURI(stmt.getPredicate());
        Value object = dialect.getNode(stmt.getObject());
        URI context = stmt.getContext() != null ? dialect.getURI(stmt.getContext()) : null;
        return dialect.createStatement(subject, predicate, object, context);
    }
    
    @Nullable
    private Resource convert(@Nullable ID id) {
        return id != null ? dialect.getResource(id) : null;
    }
    
    @Nullable
    private Value convert(@Nullable NODE node) {
        return node != null ? dialect.getNode(node) : null;
    }

    private STMT convert(Statement statement, boolean asserted) {
        UID context = statement.getContext() != null ? (UID)dialect.getID(statement.getContext()) : null;
        return new STMT(
                dialect.getID(statement.getSubject()), 
                dialect.getUID(statement.getPredicate()), 
                dialect.getNODE(statement.getObject()), 
                context, asserted);
    }

    @Nullable
    private URI convert(@Nullable UID uid) {
        return uid != null ? dialect.getURI(uid) : null;
    }

    @Override
    public BID createBNode() {
        return dialect.getBID(dialect.createBNode());
    }

    @Override
    public BeanQuery createQuery(Session session) {
        SesameQuery query = new SesameQuery(
                session, 
                dialect, 
                connection, 
                StatementPattern.Scope.DEFAULT_CONTEXTS,
                datatypeInference);
        query.getMetadata().setDistinct(true);
        return query;
    }
    
    @Override
    public <Q> Q createQuery(Session session, Class<Q> queryType) {
        throw new RuntimeException();
    }
    
    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, final boolean includeInferred) {
        final ModelResult statements = 
            findStatements(
                    convert(subject), 
                    convert(predicate), 
                    convert(object), 
                    includeInferred,
                    convert(context));
        return new CloseableIterator<STMT>() {

            @Override
            public void close() throws IOException {
                try {
                    statements.close();
                } catch (StoreException e1) {
                    throw new IOException(e1);
                }
            }

            @Override
            public boolean hasNext() {
                try {
                    return statements.hasNext();
                } catch (StoreException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public STMT next() {
                try {
                    return convert(statements.next(), !includeInferred);
                } catch (StoreException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    private ModelResult findStatements(Resource subject, URI predicate, Value object, boolean includeInferred, URI context) {
        try {
            if (context == null) {
                return connection.match(subject, predicate, object, includeInferred);
            } else if (includeInferred) {
                return connection.match(subject, predicate, object, includeInferred, context, null);
            } else {
                return connection.match(subject, predicate, object, includeInferred, context);
            }
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }    

    public RepositoryConnection getConnection(){
        return connection;
    }

    public Dialect<Value, Resource, BNode, URI, Literal, Statement> getDialect() {
        return dialect;
    }
    
    public RDFBeanTransaction getTransaction() {
        return localTxn;
    }

    @Override
    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {
        if (!readonlyTnx){
            try {
                if (removedStatements != null) {
                    connection.remove(convert(removedStatements));
                }
                if (addedStatements != null) {
                    connection.add(convert(addedStatements));
                }
            } catch (StoreException e) {
                throw new RuntimeException(e);
            }    
        }        
    }


}
