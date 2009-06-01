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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.BeanQueryAdapter;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.query.SesameQuery;

/**
 * @author sasa
 * 
 */
public class SesameConnection implements RDFConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(SesameConnection.class);
    
    private RepositoryConnection connection;
    
    private SesameDialect dialect;
    
    private SesameTransaction localTxn = null;
    
    private boolean roTnx = false;

    private ValueFactory vf;
    
    public SesameConnection(RepositoryConnection connection) {
        this.connection = connection;
        this.vf = connection.getValueFactory();
        this.dialect = new SesameDialect(vf);
    }

    public void cleanUpAfterCommit(){
        localTxn = null;
        roTnx = false;
    }
    
    public void cleanUpAfterRollback(){
        localTxn = null;
        roTnx = false;
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
        if (readOnly) {
            localTxn.setRollbackOnly();
        }
        roTnx = readOnly;
        localTxn.begin();
        return localTxn;
    }

    private ModelResult findStatements(Resource subject, URI predicate, Value object, boolean includeInferred, URI context) {
        ModelResult statements = null;
        try {
            if (context == null) {
                statements = connection.match(subject, predicate, object, includeInferred);
            } else if (includeInferred) {
                statements = connection.match(subject, predicate, object, includeInferred, context, null);
            } else {
                statements = connection.match(subject, predicate, object, includeInferred, context);
            }
            return statements;
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
        SesameQuery query = new SesameQuery(session, dialect, connection, StatementPattern.Scope.DEFAULT_CONTEXTS);
        query.getMetadata().setDistinct(true);
        return new BeanQueryAdapter(query,query);
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
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
                    return convert(statements.next());
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

    private STMT convert(Statement statement) {
        return new STMT(
                convert(statement.getSubject()), 
                convert(statement.getPredicate()), 
                convert(statement.getObject()), 
                (UID) convert(statement.getContext())
            );
    }
    
    private NODE convert(Value value) {
        if (value instanceof Literal) {
            return dialect.getLIT((Literal) value);
        } else {
            return convert((Resource) value);
        }
    }

    private ID convert(Resource resource) {
        if (resource == null) {
            return null; 
        } else if (resource instanceof URI) {
            return convert((URI) resource);
        } else {
            return new BID(((BNode) resource).getID());
        }
    }

    private UID convert(URI uri) {
        return new UID(uri.getNamespace(), uri.getLocalName());
    }

    private URI convert(UID uid) {
        return uid != null ? vf.createURI(uid.getId()) : null;
    }
    
    private Resource convert(ID id) {
        if (id == null) {
            return null;
        } else if (id.isBNode()) {
            return vf.createBNode(id.getId());
        } else {
            return convert((UID) id);
        }
    }

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
        if (!roTnx){
            try {
                connection.remove(convert(removedStatements));
                connection.add(convert(addedStatements));
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

}
