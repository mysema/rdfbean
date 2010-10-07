/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.*;
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.ModelResult;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.ontology.Ontology;
import com.mysema.rdfbean.sesame.query.DirectQuery;
import com.mysema.rdfbean.sesame.query.SesameQuery;

/**
 * SaesameConnection is the RDFConnection implementation for RepositoryConnection usage
 * 
 * @author sasa
 * 
 */
public class SesameConnection implements RDFConnection {

    private static final URI RDF_TYPE = org.openrdf.model.vocabulary.RDF.TYPE;

    private static final Var RDF_TYPE_VAR = new Var("rdf_type", RDF_TYPE);;
    
    private static final Var SUBJECT_VAR = new Var("subject");
    
    private static final ExtensionElem extensionElem = new ExtensionElem(new ValueConstant(RDF_TYPE),"_rdf_type");
        
    private static final ProjectionElemList projections = new ProjectionElemList();
          
    static{
        RDF_TYPE_VAR.setAnonymous(true);
        projections.addElements(new ProjectionElem("subject"));
        projections.addElements(new ProjectionElem("_rdf_type", "predicate"));
        projections.addElements(new ProjectionElem("object"));
    }
    
    private final RepositoryConnection connection;
    
    private final SesameDialect dialect;
    
    private final Inference inference;
    
    @Nullable
    private SesameTransaction localTxn = null;

    private final Ontology<UID> ontology;
    
    private boolean readonlyTnx = false;
    
    private final Transformer<STMT,Statement> stmtTransformer = new Transformer<STMT,Statement>(){
        @Override
        public Statement transform(STMT stmt) {
            return convert(stmt);
        }        
    };
    
    private final ValueFactory vf;
    
    private final SesameRepository repository;
    
    public SesameConnection(SesameRepository repository, RepositoryConnection connection, Ontology<UID> ontology, Inference inference) {
        this.repository = Assert.notNull(repository,"repository");
        this.connection = Assert.notNull(connection,"connection");
        this.vf = connection.getValueFactory();
        this.dialect = new SesameDialect(vf);
        this.ontology = Assert.notNull(ontology,"ontology");
        this.inference = Assert.notNull(inference,"inference");
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
        return new Iterable<Statement>(){
            @Override
            public Iterator<Statement> iterator() {
                return new TransformIterator<STMT,Statement>(stmts.iterator(),stmtTransformer);
            }
            
        };
    }
    
    @Nullable
    private Resource convert(@Nullable ID id) {
        return id != null ? dialect.getResource(id) : null;
    }
    
    @Nullable
    private Value convert(@Nullable NODE node) {
        return node != null ? dialect.getNode(node) : null;
    }
    
    private Statement convert(STMT stmt){
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

    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        throw new UnsupportedOperationException();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
        if (queryLanguage.equals(QueryLanguage.QUERYDSL)){
            SesameQuery query = new SesameQuery(
                    session, 
                    dialect, 
                    dialect.getValueFactory(),
                    connection, 
                    StatementPattern.Scope.DEFAULT_CONTEXTS,
                    ontology,
                    inference);
            query.getMetadata().setDistinct(true);
            return (Q)query;
            
        }else if (queryLanguage.equals(QueryLanguage.SPARQL)){
            try {
                Query query = connection.prepareQuery(org.openrdf.query.QueryLanguage.SPARQL, definition.toString());
                if (query instanceof GraphQuery){
                    return (Q)new GraphQueryImpl((GraphQuery)query, dialect);
                }else if (query instanceof TupleQuery){
                    return (Q)new TupleQueryImpl((TupleQuery)query, dialect);
                }else{
                    throw new RepositoryException("Unsupported query type " + query.getClass().getName());
                }               
            } catch (StoreException e) {
                throw new QueryException(e);
            } catch (MalformedQueryException e) {
                throw new QueryException(e);
            }
            
        }else{
            throw new UnsupportedQueryLanguageException(queryLanguage);
        }
    }

    private ModelResult findOfType(Collection<UID> types, @Nullable URI context, boolean includeInferred){        
        try {
            List<StatementPattern> patterns = new ArrayList<StatementPattern>();
            Var contextVar = context != null ? new Var("context", context) : null; 
            for (UID type : types){
                Var objectVar = new Var("object", dialect.getURI(type));
                patterns.add(new StatementPattern(SUBJECT_VAR, RDF_TYPE_VAR, objectVar, contextVar));            
            }
            Union union = new Union(patterns);
            Extension extension = new Extension(union, extensionElem);            
            Projection projection = new Projection(extension, projections);
            Reduced reduced = new Reduced(projection);            
            return DirectQuery.query(connection, new GraphQueryModel(reduced), includeInferred);
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }
    
    @Override
    public CloseableIterator<STMT> findStatements(ID sub, UID pre, NODE obj, UID con, boolean includeInferred) {        
        Resource subject = convert(sub);
        URI predicate = convert(pre);
        Value object = convert(obj);
        URI context = convert(con);
        
        // subClassOf inference
        if (subject == null && RDF.type.equals(pre) && obj instanceof UID && inference.subClassOf()){
            Collection<UID> types = ontology.getSubtypes((UID)obj);
            if (types.size() > 1){
                return new ModelResultIterator(dialect, 
                        findOfType(types, context, includeInferred), 
                        includeInferred);
            }
        }
        
        // TODO : subPropertyOf inference
        
        // default results
        return new ModelResultIterator(dialect, 
                findStatements(subject, predicate, object, includeInferred, context), 
                includeInferred);       
    }    

    private ModelResult findStatements(
            @Nullable Resource subject, @Nullable URI predicate, @Nullable Value object, 
            boolean includeInferred, @Nullable URI context) {
        try {
            if (context == null) {
                return connection.match(subject, predicate, object, includeInferred);
            } else if (includeInferred) {
                return connection.match(subject, predicate, object, includeInferred, context, null);
            } else {
                return connection.match(subject, predicate, object, includeInferred, context);
            }
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    public RepositoryConnection getConnection(){
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
                throw new RepositoryException(e);
            }    
        }        
    }


}
