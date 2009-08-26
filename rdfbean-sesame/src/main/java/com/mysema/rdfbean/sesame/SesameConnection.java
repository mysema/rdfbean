/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.openrdf.model.*;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.ModelResult;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.BeanQueryAdapter;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.query.OperationMappings;
import com.mysema.rdfbean.sesame.query.SesameQuery;

/**
 * @author sasa
 * 
 */
public class SesameConnection implements RDFConnection {
    
    private static final OperationMappings sesameOps = new OperationMappings();
    
    private final RepositoryConnection connection;
    
    private final SesameDialect dialect;
    
    @Nullable
    private SesameTransaction localTxn = null;
    
    private boolean readonlyTnx = false;

    private final ValueFactory vf;
    
    public SesameConnection(RepositoryConnection connection) {
        this.connection = connection;
        this.vf = connection.getValueFactory();
        this.dialect = new SesameDialect(vf);        
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

    @Override
    public RDFBeanTransaction beginTransaction(Session session, boolean readOnly, int txTimeout, int isolationLevel) {
        localTxn = new SesameTransaction(this, isolationLevel);        
        readonlyTnx = readOnly;
        localTxn.begin();
        return localTxn;
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
                sesameOps);
        query.getMetadata().setDistinct(true);
        return new BeanQueryAdapter(query,query);
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, final boolean includeInferred) {
        final ModelResult statements = 
            findStatements(convert(subject), convert(predicate), convert(object), 
                    includeInferred, convert(context));
        return new CloseableIterator<STMT>() {

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

            @Override
            public void close() throws IOException {
                try {
                    statements.close();
                } catch (StoreException e1) {
                    throw new IOException(e1);
                }
            }
        };
    }

    private STMT convert(Statement statement, boolean asserted) {
        return new STMT(
                convert(statement.getSubject()), 
                convert(statement.getPredicate()), 
                convert(statement.getObject()), 
                (UID) convert(statement.getContext()),
                asserted
            );
    }
    
    private NODE convert(Value value) {
        if (value instanceof Literal) {
            return dialect.getLIT((Literal) value);
        } else {
            return convert((Resource) value);
        }
    }

    @Nullable
    private ID convert(Resource resource) {
        if (resource == null) {
            return null; 
        } else {
            return dialect.getID(resource);
        }
    }

    @Nullable
    private UID convert(URI uri) {
        if (uri == null) {
            return null;
        } else {
            return dialect.getUID(uri);
        }
    }

    @Nullable
    private URI convert(UID uid) {
        return uid != null ? vf.createURI(uid.getId()) : null;
    }
    
    @Nullable
    private Resource convert(ID id) {
        if (id == null) {
            return null;
        } else if (id.isBNode()) {
            return vf.createBNode(id.getId());
        } else {
            return convert((UID) id);
        }
    }

    @Nullable
    private Value convert(NODE node) {
        Value value = null;
        if (node != null) {
            if (node.isLiteral()) {
                value = dialect.getLiteral((LIT) node);
            } else {
                value = convert((ID) node);
            }
        }
        return value;
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
    
    private Collection<Statement> convert(Collection<STMT> stmts) {
        List<Statement> statements = new ArrayList<Statement>(stmts.size());
        for (STMT stmt : stmts) {
            Value object = stmt.isObjectStatement() 
                ? dialect.getResource((ID) stmt.getObject())
                : dialect.getLiteral((LIT) stmt.getObject());
            statements.add(vf.createStatement(
                    dialect.getResource(stmt.getSubject()), 
                    dialect.getURI(stmt.getPredicate()), 
                    object,
                    stmt.getContext() != null ? dialect.getURI(stmt.getContext()) : null
                    ));
        }
        return statements;
    }

    @Override
    public void clear() {
    }

}
