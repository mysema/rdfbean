/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.mulgara;

import static com.mysema.rdfbean.mulgara.Constants.EMPTY_GRAPH;
import static com.mysema.rdfbean.mulgara.Constants.O_VAR;
import static com.mysema.rdfbean.mulgara.Constants.P_VAR;
import static com.mysema.rdfbean.mulgara.Constants.S_VAR;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.MultiMap;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Triple;
import org.mulgara.client.jrdf.GraphElementBuilder;
import org.mulgara.connection.Connection;
import org.mulgara.query.Answer;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.GraphResource;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.SelectElement;
import org.mulgara.query.TuplesException;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.operation.Deletion;
import org.mulgara.query.operation.Insertion;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.UnsupportedQueryLanguageException;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SimpleBeanQuery;
import com.mysema.util.MultiMapFactory;

/**
 * MulgaraConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MulgaraConnection implements RDFConnection{
    
    private static final Logger logger = LoggerFactory.getLogger(MulgaraConnection.class);
    
    private final Connection connection;
    
    private final MulgaraDialect dialect;
    
    private final GraphElementFactory elementFactory;

    private MulgaraTransaction localTxn;

    private boolean readonlyTnx = false;
    
    public MulgaraConnection(Connection connection) {        
        try {
            this.connection = Assert.notNull(connection,"connection");
            this.elementFactory = new GraphElementBuilder();
            this.dialect = new MulgaraDialect(elementFactory);
        } catch (GraphException e) {
            throw new RepositoryException( e);
        }
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout,
            int isolationLevel) {
        localTxn = new MulgaraTransaction(this, connection, readOnly, txTimeout, isolationLevel);
        readonlyTnx = readOnly;
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
    public void close(){
        try {
            connection.close();
        } catch (QueryException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RepositoryException(error, e);
        }
        
    }

    private Triple convert(STMT stmt){        
        return dialect.createStatement(
                dialect.getResource(stmt.getSubject()), 
                dialect.getURI(stmt.getPredicate()),
                dialect.getNode(stmt.getObject()),
                stmt.getContext() != null ? dialect.getURI(stmt.getContext()) : null);
    }
    
    @Override
    public BID createBNode() {
        return dialect.getBID(dialect.createBNode());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
        if (queryLanguage.equals(QueryLanguage.QUERYDSL)){
            return (Q) new SimpleBeanQuery(session);
        }else{
            throw new UnsupportedQueryLanguageException(queryLanguage);
        }
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        List<SelectElement> variableList = new ArrayList<SelectElement>();        
        if (subject == null){
            variableList.add(S_VAR);
        }
        if (predicate == null){
            variableList.add(P_VAR);
        }
        if (object == null){
            variableList.add(O_VAR);
        }
        URI contextURI = context != null ? URI.create(context.getId()) : EMPTY_GRAPH;
        GraphExpression graphExpression = new GraphResource(contextURI);        
        // FIXME!
        ConstraintExpression constraintExpression = new ConstraintImpl(
                (ConstraintElement)(subject != null ? dialect.getResource(subject) : S_VAR),
                (ConstraintElement)(predicate != null ? dialect.getURI(predicate) : P_VAR),
                (ConstraintElement)(object != null ? dialect.getNode(object) : O_VAR),
                new URIReferenceImpl(contextURI));
        Query query = new Query(variableList,
                graphExpression, 
                constraintExpression, 
                null, /* constraintHaving */
                Collections.<Order>emptyList(), /* order */ 
                null, /* limit */ 
                0,  /* offset */
                new UnconstrainedAnswer());
        
        try {
            Answer answer = connection.execute(query);
            return new MulgaraResultIterator(dialect, answer, subject, predicate, object, context);
        } catch (QueryException e) {
            throw new RepositoryException(e);
        } catch (TuplesException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void update(Set<STMT> removedStatements, Set<STMT> addedStatements) {        
        if (!readonlyTnx){
            try {
                // group by context
                MultiMap<URI,Triple> removed = MultiMapFactory.<URI,Triple>createWithSet();
                MultiMap<URI,Triple> added = MultiMapFactory.<URI,Triple>createWithSet();
                for (STMT stmt : removedStatements){
                    URI context = stmt.getContext() != null ? URI.create(stmt.getContext().getId()) : EMPTY_GRAPH;
                    removed.put(context, convert(stmt));
                }
                for (STMT stmt : addedStatements){
                    URI context = stmt.getContext() != null ? URI.create(stmt.getContext().getId()) : EMPTY_GRAPH;
                    added.put(context, convert(stmt));
                }
                
                // apply deletions
                for (Map.Entry<URI, Collection<Triple>> entry : removed.entrySet()){
                    connection.execute(new Deletion(entry.getKey(), (Set<Triple>)entry.getValue()));
                }                
                // apply insertions
                for (Map.Entry<URI, Collection<Triple>> entry : added.entrySet()){
                    connection.execute(new Insertion(entry.getKey(), (Set<Triple>)entry.getValue()));
                }
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public long getNextLocalId() {
        throw new UnsupportedOperationException();
    }

}
