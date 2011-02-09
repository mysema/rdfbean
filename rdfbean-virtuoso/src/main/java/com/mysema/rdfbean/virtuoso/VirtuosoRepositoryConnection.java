package com.mysema.rdfbean.virtuoso;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryMetadata;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.model.io.SPARQLUpdateWriter;
import com.mysema.rdfbean.model.io.TurtleStringWriter;

/**
 * @author tiwe
 *
 */
public class VirtuosoRepositoryConnection implements RDFConnection {
    
    private static final Map<QueryLanguage<?,?>, SPARQLQuery.ResultType> resultTypes = new HashMap<QueryLanguage<?,?>, SPARQLQuery.ResultType>();
    
    static{
        resultTypes.put(QueryLanguage.BOOLEAN, SPARQLQuery.ResultType.BOOLEAN);
        resultTypes.put(QueryLanguage.GRAPH, SPARQLQuery.ResultType.TRIPLES);
        resultTypes.put(QueryLanguage.TUPLE, SPARQLQuery.ResultType.TUPLES);        
    }
    
    private static final Logger logger = LoggerFactory.getLogger(VirtuosoRepository.class);
    
    private static final int BATCH_SIZE = 5000;
    
    private static final String DEFAULT_OUTPUT = "sparql\n ";

    private static final String INTERNAL_PREFIX = "http://www.openlinksw.com/";
    
    private static final String JAVA_OUTPUT = "sparql define output:format '_JAVA_'\n ";

    private static final String SPARQL_CREATE_GRAPH = "sparql create silent graph iri(??)";
    
//    private static final String SPARQL_SELECT_GRAPHS = "sparql select distinct ?g where { graph ?g { ?s ?p ?o } }";
    
    private static final String SPARQL_SELECT_KNOWN_GRAPHS = "DB.DBA.SPARQL_SELECT_KNOWN_GRAPHS()";
    
    private static final String SPARQL_DROP_GRAPH = "sparql drop silent graph iri(??)";
    
//    private static final String SPARQL_CLEAR_GRAPH = "sparql clear graph iri(??)";
    
    private static final String SPARQL_DELETE = "sparql define output:format '_JAVA_' " +
    		"delete from graph iri(??) {`iri(??)` `iri(??)` " +
    		"`bif:__rdf_long_from_batch_params(??,??,??)`}";

    public static void bindBlankNode(PreparedStatement ps, int col, BID n) throws SQLException {
        ps.setString(col, "_:" + n.getValue());
    }

    public static void bindResource(PreparedStatement ps, int col, ID n) throws SQLException {
        if (n.isURI()){
            bindURI(ps, col, n.asURI());
        }else if (n.isBNode()){
            bindBlankNode(ps, col, n.asBNode());
        }else{
            throw new IllegalArgumentException(n.toString());
        }
    }

    public static void bindURI(PreparedStatement ps, int col, UID n) throws SQLException {
        ps.setString(col, n.getValue());
    }
    
    public static void bindValue(PreparedStatement ps, int col, NODE n) throws SQLException {
        if (n.isURI()) {
            ps.setInt(col, 1);
            ps.setString(col + 1, n.getValue());
            ps.setNull(col + 2, java.sql.Types.VARCHAR);

        } else if (n.isBNode()) {
            ps.setInt(col, 1);
            ps.setString(col + 1, "_:" + n.getValue());
            ps.setNull(col + 2, java.sql.Types.VARCHAR);

        } else if (n.isLiteral()) {
            LIT lit = n.asLiteral();
            if (lit.getLang() != null) {
                ps.setInt(col, 5);
                ps.setString(col + 1, lit.getValue());
                ps.setString(col + 2, LocaleUtil.toLang(lit.getLang()));
            } else {
                ps.setInt(col, 4);
                ps.setString(col + 1, lit.getValue());
                ps.setString(col + 2, lit.getDatatype().getId());
            }
        } else {
            throw new IllegalArgumentException(n.toString());
        }
    }
    
    private final IdSequence idSequence;

    private final Collection<UID> allowedGraphs;
    
    private final Connection connection;
    
    private final Converter converter;

    private final UID defaultGraph;
    
    private final int prefetchSize;
    
    protected VirtuosoRepositoryConnection(
            IdSequence idSequence,
            Converter converter, 
            int prefetchSize, 
            UID defGraph,
            Collection<UID> allowedGraphs,
            Connection connection) {
        this.idSequence = idSequence;
        this.converter = converter;
        this.connection = connection;
        this.prefetchSize = prefetchSize;
        this.defaultGraph = defGraph;
        this.allowedGraphs = allowedGraphs;
    }

    public void addBulk(Collection<STMT> addedStatements) throws SQLException, IOException {
        verifyNotReadOnly();
        
        Map<UID, TurtleStringWriter> writers = new HashMap<UID, TurtleStringWriter>();
        
        // write statements to writers
        for (STMT stmt : addedStatements) {
            assertAllowedGraph(stmt.getContext());
            UID context = stmt.getContext() != null ? stmt.getContext() : defaultGraph;
            TurtleStringWriter writer = writers.get(context);
            if (writer == null){
                writer = new TurtleStringWriter(true);
                writers.put(context, writer);       
                writer.begin();
            }            
            writer.handle(stmt);
        }

        // create graphs
        PreparedStatement stmt = connection.prepareStatement(SPARQL_CREATE_GRAPH);
        try{
            for (UID graph : writers.keySet()){
                stmt.setString(1, graph.getId());
                stmt.execute();
                stmt.clearParameters();
            }
        }finally{
            stmt.close();
        }
        
        // load data
        stmt = connection.prepareStatement("DB.DBA.TTLP(?,'',?,0)");
        try{
            for (Map.Entry<UID, TurtleStringWriter> entry : writers.entrySet()){
                entry.getValue().end();
                stmt.setString(1, entry.getValue().toString());
                stmt.setString(2, entry.getKey().getId());
                stmt.execute();
                stmt.clearParameters();                
            }
        }finally{
            stmt.close();
        }
                
    }
    
    public void removeBulk(Collection<STMT> deletedStatements) throws SQLException, IOException {
        verifyNotReadOnly();
        
        Map<UID, SPARQLUpdateWriter> writers = new HashMap<UID, SPARQLUpdateWriter>();
        
        // write statements to writers
        for (STMT stmt : deletedStatements) {
            assertAllowedGraph(stmt.getContext());
            UID context = stmt.getContext() != null ? stmt.getContext() : defaultGraph;
            SPARQLUpdateWriter writer = writers.get(context);
            if (writer == null){
                writer = new SPARQLUpdateWriter(context, true);
                writers.put(context, writer);      
                writer.begin();
            }            
            writer.handle(stmt);
        }
        
        // load data
        Statement stmt = connection.createStatement();
        try{
            for (Map.Entry<UID, SPARQLUpdateWriter> entry : writers.entrySet()){
                entry.getValue().end();
                stmt.execute("sparql " + entry.getValue().toString()); // NOSONAR                
            }
        }finally{
            stmt.close();
        }
                
    }

    private void assertAllowedGraph(@Nullable UID context) {
        if (context != null && !isAllowedGraph(context)){
            throw new IllegalStateException("Context not allowed for update " + context.getId());
        }
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        return new VirtuosoTransaction(connection, readOnly, txTimeout, isolationLevel);
    }

    private void bindNodes(PreparedStatement ps, List<NODE> nodes) throws SQLException{
        int offset = 1;
        for (NODE node : nodes){
            if (node.isResource()){
                bindResource(ps, offset++, node.asResource());
            }else{
                bindValue(ps, offset, node);
                offset += 3;
            }
        }
    }

    @Override
    public void clear() {
        // ?!?
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public BID createBNode() {
        return new BID();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        if (queryLanguage.equals(QueryLanguage.SPARQL)){
            String query = definition.toString();
            SPARQLQuery.ResultType resultType = getResultType(query);                
            return (Q)createSPARQLQuery(query, resultType);         
            
        }else if (queryLanguage.equals(QueryLanguage.BOOLEAN) ||
                  queryLanguage.equals(QueryLanguage.GRAPH) ||
                  queryLanguage.equals(QueryLanguage.TUPLE)){    
            SPARQLVisitor visitor = new SPARQLVisitor();
            QueryMetadata md = (QueryMetadata)definition;
            visitor.visit(md, queryLanguage);
            SPARQLQuery query = createSPARQLQuery(visitor.toString(), resultTypes.get(queryLanguage));
            visitor.addBindings(query, md);
            return (Q)query;
            
        }else{
            throw new IllegalArgumentException("Unsupported query language " + queryLanguage);
        }
    }

    private SPARQLQuery createSPARQLQuery(String query, SPARQLQuery.ResultType resultType) {
        if (resultType == SPARQLQuery.ResultType.BOOLEAN){
            return new BooleanQueryImpl(connection, prefetchSize, JAVA_OUTPUT + query);
        }else if (resultType == SPARQLQuery.ResultType.TUPLES){
            return new TupleQueryImpl(connection, converter, prefetchSize, DEFAULT_OUTPUT + query);
        }else if (resultType == SPARQLQuery.ResultType.TRIPLES){
            return new GraphQueryImpl(connection, converter, prefetchSize, JAVA_OUTPUT + query);
        }else{
            throw new IllegalArgumentException("No result type for " + query);
        }
    }
 
    @Override
    public boolean exists(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        STMTIterator stmts = findStatements(subject, predicate, object, context, includeInferred, true);
        try{
            return stmts.hasNext();
        }finally{
            stmts.close();
        }
    }

    @Override
    public CloseableIterator<STMT> findStatements(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object, 
            @Nullable UID context, boolean includeInferred) {
        return findStatements(subject, predicate, object, context, includeInferred, false);
    }

    private STMTIterator findStatements(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object, 
            @Nullable UID context, boolean includeInferred, boolean hasOnly) {
        
        List<NODE> nodes = new ArrayList<NODE>(8);
        String s = "?s", p = "?p", o = "?o";
        
//        if (context != null){         
//            nodes.add(context);
//        }
        if (subject != null){
            nodes.add(subject);
            s = "`iri(??)`";
        }
        if (predicate != null){
            nodes.add(predicate);
            p = "`iri(??)`";
        }
        if (object != null){
            nodes.add(object);
            if (object.isResource()){
                o = "`iri(??)`";
            }else{
                o = "`bif:__rdf_long_from_batch_params(??,??,??)`";
            }
        }
        
        // query construction
        StringBuffer query = new StringBuffer("sparql select * ");
        if (context != null){
            query.append("from named <" + context.getId() + "> ");
//            query.append("from named iri(??) ");
        }
        query.append("where { graph ?g { " + s + " " + p + " " + o + " } }");        
        if (hasOnly){
            query.append(" limit 1");
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(query.toString());
            bindNodes(ps, nodes);
            ps.setFetchSize(prefetchSize);            
            rs = ps.executeQuery();
            return new STMTIterator(converter, ps, rs, subject, predicate, object, defaultGraph);
        } catch (SQLException e) {
            AbstractQueryImpl.close(ps, rs); //NOSONAR
            throw new RepositoryException("Query execution failed : " + query.toString(), e);
        }        
    }

    public Connection getConnection(){
        return connection;
    }

    @Override
    public long getNextLocalId() {
        return idSequence.getNextId();
    }

    private SPARQLQuery.ResultType getResultType(String definition){
        String normalized = definition.toLowerCase().replaceAll("\\s+", " ");
        if (normalized.startsWith("select ") || normalized.contains(" select")){
            return SPARQLQuery.ResultType.TUPLES;
        }else if (normalized.startsWith("ask ") || normalized.contains(" ask ")){
            return SPARQLQuery.ResultType.BOOLEAN;
        }else if (normalized.startsWith("construct ") || normalized.contains(" construct ")){
            return SPARQLQuery.ResultType.TRIPLES;
        }else if (normalized.startsWith("describe ") || normalized.contains(" describe ")){
            return SPARQLQuery.ResultType.TRIPLES;
        }else{
            throw new IllegalArgumentException("Illegal query " + definition);
        }
    }

    private boolean isAllowedGraph(UID context){
        return !context.getId().startsWith(INTERNAL_PREFIX)        
            && (context.equals(defaultGraph) 
            || allowedGraphs.isEmpty() 
            || allowedGraphs.contains(context));
    }

    public boolean isReadOnly() {
        try {
            return connection.isReadOnly();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }
    
    public void load(Format format, InputStream is, @Nullable UID context, boolean replace) throws SQLException, IOException{
        if (context != null && replace){
            remove(null, null, null, context);
        }
        PreparedStatement stmt = null;
        try{
            if (format == Format.N3 || format == Format.TURTLE || format == Format.NTRIPLES){ // UTF-8
                String content = IOUtils.toString(is, format == Format.NTRIPLES ? "US-ASCII" : "UTF-8");
                stmt = connection.prepareStatement("DB.DBA.TTLP(?,'',?,0)");
                stmt.setString(1, content);
                stmt.setString(2, context != null ? context.getId() : defaultGraph.getId());
            }else if (format == Format.RDFXML){ 
                String content = IOUtils.toString(is, "UTF-8"); // TODO : proper XML load
                stmt = connection.prepareStatement("DB.DBA.RDF_LOAD_RDFXML(?,'',?,0)");
                stmt.setString(1, content);
                stmt.setString(2, context != null ? context.getId() : defaultGraph.getId());
            }else{
                throw new IllegalArgumentException("Unsupported forma " + format);
            }
            stmt.execute();
        }finally{
             if (stmt != null){
                 stmt.close();
             }
        }
        
    }
    
    private void remove(Collection<STMT> removedStatements) throws SQLException {
        verifyNotReadOnly();

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(VirtuosoRepositoryConnection.SPARQL_DELETE);
            int count = 0;

            for (STMT stmt : removedStatements) {
                assertAllowedGraph(stmt.getContext());
                ps.setString(1, stmt.getContext() != null ? stmt.getContext().getId() : defaultGraph.getId());
                bindResource(ps, 2, stmt.getSubject());
                bindURI(ps, 3, stmt.getPredicate());
                bindValue(ps, 4, stmt.getObject());
                ps.addBatch();
                count++;

                if (count > BATCH_SIZE) {
                    ps.executeBatch();
                    ps.clearBatch();
                    count = 0;
                }
            }

            if (count > 0) {
                ps.executeBatch();
                ps.clearBatch();
            }
        }finally{
            if (ps != null){
                ps.close();
            }
        }
    }

    
    @Override
    public void remove(ID subject, UID predicate, NODE object, UID context) {
        try {
            removeMatch(subject, predicate, object, context);
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    private void removeMatch(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, 
            @Nullable UID context) throws SQLException  {
        assertAllowedGraph(context);
        PreparedStatement ps = null;
        try {
            // context given
            if (subject == null && predicate == null && object == null && context != null) {                
                ps = connection.prepareStatement(SPARQL_DROP_GRAPH);
                ps.setString(1, context.getId());
                ps.execute();    
                if (logger.isInfoEnabled()){
                    logger.info("Dropped " + context.getId());    
                }                

            // all given
            } else if (subject != null && predicate != null && object != null && context != null) {
                ps = connection.prepareStatement(VirtuosoRepositoryConnection.SPARQL_DELETE);
                ps.setString(1, context.getId());
                bindResource(ps, 2, subject);
                bindURI(ps, 3, predicate);
                bindValue(ps, 4, object);
                ps.execute();
                
            // no context
            } else if (context == null){
                Set<UID> graphs = new HashSet<UID>();
                graphs.add(defaultGraph);
                
                // collect graphs
                ps = connection.prepareStatement(SPARQL_SELECT_KNOWN_GRAPHS);
                ps.setFetchSize(25);
                ResultSet rs = null;                    
                try{
                    rs = ps.executeQuery();
                    while (rs.next()){
                        UID graph = new UID(rs.getString(1));
                        if (isAllowedGraph(graph)){
                            graphs.add(graph);    
                        }                                                    
                    }                            
                }finally{
                    AbstractQueryImpl.close(ps, rs);
                    ps = null;
                }
                                
                for (UID graph : graphs){
                    removeMatch(subject, predicate, object, graph);
                }

            } else {
                String s = "?s", p = "?p", o = "?o", c = "iri(??)";
                List<NODE> nodes = new ArrayList<NODE>(8);
                nodes.add(context);                 
                if (subject != null){
                    nodes.add(subject);
                    s = "`iri(??)`";
                }
                if (predicate != null){
                    nodes.add(predicate);
                    p = "`iri(??)`";
                }
                if (object != null){
                    nodes.add(object);
                    if (object.isResource()){
                        o = "`iri(??)`";
                    }else{
                        o = "`bif:__rdf_long_from_batch_params(??,??,??)`";
                    }
                }
                
                nodes.add(context);                 
                if (subject != null){
                    nodes.add(subject);
                }
                if (predicate != null){
                    nodes.add(predicate);
                }
                if (object != null){
                    nodes.add(object);
                }

                String delete = String.format("sparql delete from %1$s { %2$s %3$s %4$s } " +
                        "where { graph `%1$s` { %2$s %3$s %4$s } }", c, s, p, o);
                
                ps = connection.prepareStatement(delete);
                bindNodes(ps, nodes);
                ps.execute();
            }
        }finally{
            if (ps != null){
                ps.close();
            }
        }
    }

    @Override
    public void update(Collection<STMT> removedStatements, Collection<STMT> addedStatements) {
        if (removedStatements != null && !removedStatements.isEmpty()){
            try {
                remove(removedStatements);
            } catch (SQLException e) {
                throw new RepositoryException(e);
            }
        }
        if (addedStatements != null && !addedStatements.isEmpty()){
            try {
                addBulk(addedStatements);
            } catch (SQLException e) {
                throw new RepositoryException(e);
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
    }

    protected void verifyNotReadOnly(){
        if (isReadOnly()) {
            throw new RepositoryException("Connection is in read-only mode");
        }
    }


    @Override
    public QueryOptions getQueryOptions() {
        return QueryOptions.DEFAULT;
    }
}
