/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.sesame;

import java.util.Collection;

import javax.annotation.Nullable;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.query.QueryMetadata;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.InferenceOptions;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.QueryOptions;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.SPARQLUpdateClause;
import com.mysema.rdfbean.model.SPARQLVisitor;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.UnsupportedQueryLanguageException;
import com.mysema.rdfbean.model.UpdateLanguage;

/**
 * SaesameConnection is the RDFConnection implementation for
 * RepositoryConnection usage
 * 
 * @author sasa
 * 
 */
public class SesameConnection implements RDFConnection {

    private static final URI RDF_TYPE = org.openrdf.model.vocabulary.RDF.TYPE;

    private static final Var RDF_TYPE_VAR = new Var("rdf_type", RDF_TYPE);;

    private static final ProjectionElemList projections = new ProjectionElemList();

    static {
        RDF_TYPE_VAR.setAnonymous(true);
        projections.addElements(new ProjectionElem("subject"));
        projections.addElements(new ProjectionElem("_rdf_type", "predicate"));
        projections.addElements(new ProjectionElem("object"));
    }

    private final RepositoryConnection connection;

    private final SesameDialect dialect;

    @Nullable
    private SesameTransaction localTxn = null;

    private boolean readonlyTnx = false;

    private final Function<STMT, Statement> stmtTransformer = new Function<STMT, Statement>() {
        @Override
        public Statement apply(STMT stmt) {
            return convert(stmt);
        }
    };

    private final ValueFactory vf;

    private final SesameRepository repository;

    private final InferenceOptions inference;
    
    private final boolean serializeQueries;

    public SesameConnection(SesameRepository repository, RepositoryConnection connection,
            InferenceOptions inference, boolean serializeQueries) {
        this.repository = Assert.notNull(repository, "repository");
        this.connection = Assert.notNull(connection, "connection");
        this.vf = connection.getValueFactory();
        this.dialect = new SesameDialect(vf);
        this.inference = inference;
        this.serializeQueries = serializeQueries;
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        localTxn = new SesameTransaction(this, isolationLevel);
        readonlyTnx = readOnly;
        localTxn.begin();
        return localTxn;
    }

    public void cleanUpAfterCommit() {
        localTxn = null;
        readonlyTnx = false;
    }

    public void cleanUpAfterRollback() {
        localTxn = null;
        readonlyTnx = false;
        close();
    }

    @Override
    public void clear() {
        dialect.clear();
    }

    @Override
    public void close() {
        try {
            if (localTxn != null) {
                localTxn.rollback();
            }
            connection.close();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

    }

    private Iterable<Statement> convert(final Collection<STMT> stmts) {
        return Iterables.transform(stmts, stmtTransformer);
    }

    @Nullable
    private Resource convert(@Nullable ID id) {
        return id != null ? dialect.getResource(id) : null;
    }

    @Nullable
    private Value convert(@Nullable NODE node) {
        return node != null ? dialect.getNode(node) : null;
    }

    private Statement convert(STMT stmt) {
        Resource subject = dialect.getResource(stmt.getSubject());
        URI predicate = dialect.getURI(stmt.getPredicate());
        Value object = dialect.getNode(stmt.getObject());
        URI context = stmt.getContext() != null ? dialect.getURI(stmt.getContext()) : null;
        return dialect.createStatement(subject, predicate, object, context);
    }

    @Nullable
    private URI convert(@Nullable UID uid) {
        return uid != null ? dialect.getURI(uid) : null;
    }

    @Override
    public BID createBNode() {
        return dialect.getBID(dialect.createBNode());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createUpdate(UpdateLanguage<D, Q> updateLanguage, D definition) {
        if (updateLanguage == UpdateLanguage.SPARQL_UPDATE) {
            return (Q) new SPARQLUpdateClause(this, definition.toString());
        } else {
            throw new UnsupportedOperationException(updateLanguage.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        boolean queryInference = !inference.subClassOf();
        if (queryLanguage.equals(QueryLanguage.SPARQL)) {
            return (Q) createSPARQLQuery((String) definition);

        } else if (serializeQueries) {
            SPARQLVisitor visitor = new SPARQLVisitor();
            QueryMetadata md = (QueryMetadata) definition;
            visitor.setInToOr(true);
            visitor.visit(md, queryLanguage);
            SPARQLQuery query = createSPARQLQuery(visitor.toString());
            visitor.addBindings(query, md);
            System.err.println(visitor.toString());
            return (Q) query;
            
        } else if (queryLanguage.equals(QueryLanguage.TUPLE)) {
            SesameRDFVisitor visitor = new SesameRDFVisitor(dialect);
            TupleExpr tuple = visitor.visit((QueryMetadata) definition, queryLanguage);
            ParsedTupleQuery queryModel = new ParsedTupleQuery(tuple);
            TupleQuery query = DirectQuery.getQuery(connection, queryModel, queryInference);
            return (Q) new TupleQueryImpl(query, dialect);

        } else if (queryLanguage.equals(QueryLanguage.GRAPH)) {
            SesameRDFVisitor visitor = new SesameRDFVisitor(dialect);
            TupleExpr tuple = visitor.visit((QueryMetadata) definition, queryLanguage);
            ParsedGraphQuery queryModel = new ParsedGraphQuery(tuple);
            GraphQuery query = DirectQuery.getQuery(connection, queryModel, queryInference);
            return (Q) new GraphQueryImpl(query, dialect);

        } else if (queryLanguage.equals(QueryLanguage.BOOLEAN)) {
            SesameRDFVisitor visitor = new SesameRDFVisitor(dialect);
            TupleExpr tuple = visitor.visit((QueryMetadata) definition, queryLanguage);
            ParsedBooleanQuery queryModel = new ParsedBooleanQuery(tuple);
            BooleanQuery query = DirectQuery.getQuery(connection, queryModel, queryInference);
            return (Q) new BooleanQueryImpl(query, dialect);

        } else {
            throw new UnsupportedQueryLanguageException(queryLanguage);
        }
    }

    private SPARQLQuery createSPARQLQuery(String queryString) {
        try {
            Query query = connection.prepareQuery(org.openrdf.query.QueryLanguage.SPARQL,
                    queryString);
            if (query instanceof BooleanQuery) {
                return new BooleanQueryImpl((BooleanQuery) query, dialect);
            } else if (query instanceof GraphQuery) {
                return new GraphQueryImpl((GraphQuery) query, dialect);
            } else if (query instanceof TupleQuery) {
                return new TupleQueryImpl((TupleQuery) query, dialect);
            } else {
                throw new RepositoryException("Unsupported query type "
                        + query.getClass().getName());
            }
        } catch (MalformedQueryException e) {
            throw new QueryException(e);
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new QueryException(e);
        }
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID sub, UID pre, NODE obj, UID con,
            boolean includeInferred) {
        Resource subject = convert(sub);
        URI predicate = convert(pre);
        Value object = convert(obj);
        URI context = convert(con);

        // default results
        return new RepositoryResultIterator(dialect, findStatements(subject, predicate, object,
                includeInferred, context), includeInferred);
    }

    @Override
    public boolean exists(@Nullable ID sub, @Nullable UID pre, @Nullable NODE obj,
            @Nullable UID con, boolean includeInferred) {
        Resource subject = convert(sub);
        URI predicate = convert(pre);
        Value object = convert(obj);
        URI context = convert(con);

        try {
            if (context == null) {
                return connection.hasStatement(subject, predicate, object, includeInferred);
            } else {
                return connection
                        .hasStatement(subject, predicate, object, includeInferred, context);
            }
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    private RepositoryResult<Statement> findStatements(@Nullable Resource subject,
            @Nullable URI predicate, @Nullable Value object, boolean includeInferred,
            @Nullable URI context) {
        try {
            if (context == null) {
                return connection.getStatements(subject, predicate, object, includeInferred);
            } else if (includeInferred) {
                return connection.getStatements(subject, predicate, object, includeInferred,
                        context, null);
            } else {
                return connection.getStatements(subject, predicate, object, includeInferred,
                        context);
            }
        } catch (org.openrdf.repository.RepositoryException e) {
            throw new RepositoryException(e);
        }
    }

    public RepositoryConnection getConnection() {
        return connection;
    }

    public Dialect<Value, Resource, BNode, URI, Literal, Statement> getDialect() {
        return dialect;
    }

    @Override
    public long getNextLocalId() {
        return repository.getNextLocalId();
    }

    public RDFBeanTransaction getTransaction() {
        return localTxn;
    }

    @Override
    public void remove(ID subject, UID predicate, NODE object, UID context) {
        Resource subj = subject != null ? dialect.getResource(subject) : null;
        URI pred = predicate != null ? dialect.getURI(predicate) : null;
        Value obj = object != null ? dialect.getNode(object) : null;
        URI cont = context != null ? dialect.getURI(context) : null;
        try {
            connection.remove(subj, pred, obj, cont);
        } catch (org.openrdf.repository.RepositoryException e) {
            try {
                connection.rollback();
            } catch (org.openrdf.repository.RepositoryException e1) {
                throw new RepositoryException(e);
            }
            throw new RepositoryException(e);
        }
    }

    @Override
    public void update(Collection<STMT> removedStatements, Collection<STMT> addedStatements) {
        if (!readonlyTnx) {
            try {
                if (localTxn == null) {
                    connection.begin();
                }
                if (removedStatements != null && !removedStatements.isEmpty()) {
                    connection.remove(convert(removedStatements));
                }
                if (addedStatements != null && !addedStatements.isEmpty()) {
                    connection.add(convert(addedStatements));
                }
                if (localTxn == null) {
                    connection.commit();
                }
            } catch (org.openrdf.repository.RepositoryException e) {
                try {
                    connection.rollback();
                } catch (org.openrdf.repository.RepositoryException e1) {
                    throw new RepositoryException(e);
                }
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public QueryOptions getQueryOptions() {
        return QueryOptions.DEFAULT;
    }

    @Override
    public InferenceOptions getInferenceOptions() {
        return inference;
    }
}
