package com.mysema.rdfbean.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.EmptyCloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.UnsupportedQueryLanguageException;
import com.mysema.rdfbean.object.Session;

/**
 * @author tiwe
 *
 */
public class JenaConnection implements RDFConnection {

    private static final CloseableIterator<STMT> EMPTY_RESULTS = new EmptyCloseableIterator<STMT>();
    
    private final Graph graph;
    
    private final Model model;
    
    private final JenaDialect dialect;
    
    public JenaConnection(Graph graph, Model model, JenaDialect dialect) {
        this.graph = graph;
        this.model = model;
        this.dialect = dialect;
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        if (graph.getTransactionHandler().transactionsSupported()){
            return new JenaTransaction(graph.getTransactionHandler());    
        }else{
            throw new UnsupportedOperationException();
        }        
    }

    @Override
    public void clear() {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing        
    }

    @Override
    public BID createBNode() {
        return dialect.getBID(dialect.createBNode());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        if (queryLanguage.equals(QueryLanguage.SPARQL)){
            return (Q)createSPARQLQuery((String)definition);
        }else{
            throw new UnsupportedQueryLanguageException(queryLanguage);    
        }
    }

    private SPARQLQuery createSPARQLQuery(String definition) {
        com.hp.hpl.jena.query.Query query = QueryFactory.create(definition) ;
        if (query.isAskType()){
            return new BooleanQueryImpl(query, model, dialect);
        }else if (query.isConstructType() || query.isDescribeType()){
            return new GraphQueryImpl(query, model, dialect);
        }else {
            return new TupleQueryImpl(query, model, dialect);
        }        
    }

    @Override
    public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
        return createQuery(queryLanguage, definition);
    }

    @Override
    public boolean exists(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        return graph.contains(convert(subject), convert(predicate), convert(object));
    }
    
    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        final ExtendedIterator<Triple> triples = graph.find(convert(subject), convert(predicate), convert(object));        
        if (triples.hasNext()){
            return new TriplesIterator(dialect, triples);
        }else{
            return EMPTY_RESULTS;
        }        
        
    }

    @Override
    public long getNextLocalId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ID subject, UID predicate, NODE object, UID context) {
        graph.getBulkUpdateHandler().remove(convert(subject), convert(predicate), convert(object));
    }

    @Override
    public void update(Collection<STMT> removedStatements, Collection<STMT> addedStatements) {
        if (removedStatements != null && !removedStatements.isEmpty()){
            graph.getBulkUpdateHandler().delete(convert(removedStatements));
        }
        if (addedStatements != null && !addedStatements.isEmpty()){
            graph.getBulkUpdateHandler().add(convert(addedStatements));
        }
        
    }
    
    private List<Triple> convert(Collection<STMT> statements){
        List<Triple> triples = new ArrayList<Triple>(statements.size());
        for (STMT stmt : statements){
            triples.add(convert(stmt));
        }
        return triples;
    }
    
    private Triple convert(STMT stmt){
        return dialect.createStatement(
            dialect.getResource(stmt.getSubject()),
            dialect.getURI(stmt.getPredicate()),
            dialect.getNode(stmt.getObject()));
            
    }

    private Node convert(@Nullable UID uid){
        return uid != null ? dialect.getURI(uid) : Node.ANY;
    }
    
    private Node convert(@Nullable ID id){
        return id != null ? dialect.getResource(id) : Node.ANY;
    }
    
    private Node convert(@Nullable NODE node){
        return node != null ? dialect.getNode(node) : Node.ANY;
    }

}
