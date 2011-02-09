package com.mysema.rdfbean.jena;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.EmptyCloseableIterator;
import com.mysema.query.QueryMetadata;
import com.mysema.rdfbean.model.*;

/**
 * @author tiwe
 *
 */
public class JenaConnection implements RDFConnection {

    private static final CloseableIterator<STMT> EMPTY_RESULTS = new EmptyCloseableIterator<STMT>();
    
    private final DatasetGraph graph;
    
    private final Dataset dataset;
    
    private final JenaDialect dialect;
    
    public JenaConnection(DatasetGraph graph, Dataset dataset, JenaDialect dialect) {
        this.graph = graph;
        this.dataset = dataset;
        this.dialect = dialect;
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
//        if (graph.getTransactionHandler().transactionsSupported()){
//            return new JenaTransaction(graph.getTransactionHandler());    
//        }else{
            throw new UnsupportedOperationException();
//        }        
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
            
        }else if (queryLanguage.equals(QueryLanguage.BOOLEAN) ||
                  queryLanguage.equals(QueryLanguage.GRAPH) || 
                  queryLanguage.equals(QueryLanguage.TUPLE)){
            SPARQLVisitor visitor = new SPARQLVisitor();
            QueryMetadata md = (QueryMetadata)definition;
            visitor.visit((QueryMetadata)definition, queryLanguage);
            SPARQLQuery query = createSPARQLQuery(visitor.toString());
            visitor.addBindings(query, md);
            return (Q)query;
            
        }else{
            throw new UnsupportedQueryLanguageException(queryLanguage);    
        }
    }

    private SPARQLQuery createSPARQLQuery(String definition) {
        com.hp.hpl.jena.query.Query query = QueryFactory.create(definition) ;
        if (query.isAskType()){
            return new BooleanQueryImpl(query, dataset, dialect);
        }else if (query.isConstructType() || query.isDescribeType()){
            return new GraphQueryImpl(query, dataset, dialect);
        }else {
            return new TupleQueryImpl(query, dataset, dialect);
        }        
    }

    @Override
    public boolean exists(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        return graph.contains(convert(context), convert(subject), convert(predicate), convert(object));
    }
    
    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        Iterator<Quad> triples = graph.find(convert(context), convert(subject), convert(predicate), convert(object));        
        if (triples.hasNext()){
            return new QuadsIterator(dialect, triples);
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
        // FIXME : deleteAny should work, but doesn't
        if (context != null){
            graph.deleteAny(convert(context), convert(subject), convert(predicate), convert(object));    
        }else{
            graph.getDefaultGraph().getBulkUpdateHandler().remove(convert(subject), convert(predicate), convert(object));
            Iterator<String> names = dataset.listNames();
            while (names.hasNext()){
                Graph named = graph.getGraph(Node.createURI(names.next()));
                named.getBulkUpdateHandler().remove(convert(subject), convert(predicate), convert(object));
            }
        }
        
    }

    @Override
    public void update(Collection<STMT> removedStatements, Collection<STMT> addedStatements) {
        // TODO : add bulk handling
        if (removedStatements != null && !removedStatements.isEmpty()){
            for (STMT stmt : removedStatements){
                graph.delete(convert(stmt));
            }
        }
        if (addedStatements != null && !addedStatements.isEmpty()){
            for (STMT stmt : addedStatements){
                graph.add(convert(stmt));
            }
        }
        
    }
    
//    private List<Quad> convert(Collection<STMT> statements){
//        List<Quad> quads = new ArrayList<Quad>(statements.size());
//        for (STMT stmt : statements){
//            quads.add(convert(stmt));
//        }
//        return quads;
//    }
    
    private Quad convert(STMT stmt){
        return dialect.createStatement(
            dialect.getResource(stmt.getSubject()),
            dialect.getURI(stmt.getPredicate()),
            dialect.getNode(stmt.getObject()),
            stmt.getContext() != null ? dialect.getURI(stmt.getContext()) : Quad.defaultGraphIRI);
            
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

    @Override
    public QueryOptions getQueryOptions() {
        return QueryOptions.DEFAULT;
    }
}
