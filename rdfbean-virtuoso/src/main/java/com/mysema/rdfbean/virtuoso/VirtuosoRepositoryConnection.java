package com.mysema.rdfbean.virtuoso;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.openrdf.model.impl.ValueFactoryImpl;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.NTriplesUtil;
import com.mysema.rdfbean.object.Session;

/**
 * @author tiwe
 *
 */
public class VirtuosoRepositoryConnection implements RDFConnection {

    private static final String SPARQL_INSERT = "sparql define output:format '_JAVA_'  " +
    		"insert into graph iri(??) { `iri(??)` `iri(??)` " +
    		"`bif:__rdf_long_from_batch_params(??,??,??)` }";

    private static final String SPARQL_DELETE = "sparql define output:format '_JAVA_' " +
    		"delete from graph iri(??) {`iri(??)` `iri(??)` " +
    		"`bif:__rdf_long_from_batch_params(??,??,??)`}";
    
    private static final String SPARQL_CLEAR_GRAPH = "sparql clear graph iri(??)";
    
    private static final String INTERNAL_PREFIX = "http://www.openlinksw.com/";
    
    private static final String SELECT_GRAPHS = "sparql select distinct ?g where { graph ?g { ?s ?p ?o } }";
    
    private static final String JAVA_OUTPUT = "sparql define output:format '_JAVA_'\n ";
    
    private static final String DEFAULT_OUTPUT = "sparql\n ";

    private static final int BATCH_SIZE = 5000;
    
    private final Converter converter;

    private final UID defaultGraph;

    private final Connection connection;

    private final int prefetchSize;
    
    private final SesameDialect dialect = new SesameDialect(new ValueFactoryImpl());
    
    private final File bulkLoadDir;

    protected VirtuosoRepositoryConnection(
            Converter converter, 
            int prefetchSize, 
            UID defGraph,
            Connection connection,
            File bulkLoadDir) {
        this.converter = converter;
        this.connection = connection;
        this.prefetchSize = prefetchSize;
        this.defaultGraph = defGraph;
        this.bulkLoadDir = bulkLoadDir;
    }

    public void addBulk(Collection<STMT> addedStatements) throws SQLException, IOException {
        verifyNotReadOnly();
        
        Map<UID,File> files = new HashMap<UID,File>();
        Map<UID,Writer> writers = new HashMap<UID,Writer>();
        
        // write statements to writers
        for (STMT stmt : addedStatements) {
            UID context = stmt.getContext() != null ? stmt.getContext() : defaultGraph;
            Writer writer = writers.get(context);
            if (writer == null){
                File file = new File(bulkLoadDir, UUID.randomUUID() + ".n3");
                file.deleteOnExit();
                files.put(context, file);
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
                writers.put(context, writer);                
            }
            
            writer.write(NTriplesUtil.toString(stmt.getSubject()) + " " 
                    + NTriplesUtil.toString(stmt.getPredicate()) + " " 
                    + NTriplesUtil.toString(stmt.getObject()) + " .\n");
        }
        
        // flush and close writers
        for (Writer writer : writers.values()){
            writer.flush();
            writer.close();
        }
        
        PreparedStatement stmt = connection.prepareStatement("ld_dir(?,?,?)");
        try{
            for (Map.Entry<UID, File> entry : files.entrySet()){
                File file = entry.getValue();
                stmt.setString(1, file.getParentFile().getAbsolutePath());
                stmt.setString(2, file.getName());
                stmt.setString(3, entry.getKey().getId());
                stmt.execute();
                stmt.clearParameters();                
            }
            stmt.execute("rdf_loader_run()");   
        }finally{
            stmt.close();
            
            // delete files
            for (File file : files.values()){
                file.delete();
            }
        }
                
    }
    
    
    private void add(Collection<STMT> addedStatements) throws SQLException {
        verifyNotReadOnly();

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(VirtuosoRepositoryConnection.SPARQL_INSERT);
            int count = 0;

            for (STMT stmt : addedStatements) {
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
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        return new VirtuosoTransaction(connection, readOnly, txTimeout, isolationLevel);
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

    public static void bindBlankNode(PreparedStatement ps, int col, BID n) throws SQLException {
        ps.setString(col, "_:" + n.getValue());
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
            if (resultType == SPARQLQuery.ResultType.BOOLEAN){
                return (Q)new BooleanQueryImpl(connection, prefetchSize, JAVA_OUTPUT + query);
            }else if (resultType == SPARQLQuery.ResultType.TUPLES){
                return (Q)new TupleQueryImpl(connection, converter, prefetchSize, DEFAULT_OUTPUT + query);
            }else if (resultType == SPARQLQuery.ResultType.TRIPLES){
                return (Q)new GraphQueryImpl(connection, converter, prefetchSize, dialect, JAVA_OUTPUT + query);
            }else{
                throw new IllegalArgumentException("No result type for " + definition);
            }         
            
        }else{
            throw new IllegalArgumentException("Unsupported query language " + queryLanguage);
        }
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

    @Override
    public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
        return createQuery(queryLanguage, definition);
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
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        return findStatements(subject, predicate, object, context, includeInferred, false);
    }

    private STMTIterator findStatements(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object, 
            @Nullable UID context, 
            boolean includeInferred, boolean hasOnly) {
        
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
            AbstractQueryImpl.close(ps, rs);
            throw new RepositoryException("Query execution failed : " + query.toString(), e);
        }        
    }

    @Override
    public long getNextLocalId() {
        throw new UnsupportedOperationException("getNextLocalId has not been implemented");
    }

    public boolean isReadOnly() {
        try {
            return connection.isReadOnly();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

    private void remove(Collection<STMT> removedStatements) throws SQLException {
        verifyNotReadOnly();

        for (STMT stmt : removedStatements){
            UID context = stmt.getContext() != null ? stmt.getContext() : defaultGraph;
            removeMatch(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), context);
        }
    }

    private void removeMatch(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, 
            @Nullable UID context) throws SQLException  {
        PreparedStatement ps = null;
        try {
            // context given
            if (subject == null && predicate == null && object == null && context != null) { 
                ps = connection.prepareStatement(SPARQL_CLEAR_GRAPH);
                ps.setString(1, context.getId());
                ps.execute();

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
                List<UID> graphs = new ArrayList<UID>();
                graphs.add(defaultGraph);
                
                // collect graphs
                ps = connection.prepareStatement(SELECT_GRAPHS);
                ps.setFetchSize(25);
                ResultSet rs = null;                    
                try{
                    rs = ps.executeQuery();
                    while (rs.next()){
                        String graph = rs.getString(1);
                        if (!graph.startsWith(INTERNAL_PREFIX)){
                            graphs.add(new UID(graph));    
                        }                                                    
                    }                            
                }finally{
                    AbstractQueryImpl.close(ps, rs);
                    ps = null;
                }
                
                // delete from graphs
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
    public void remove(ID subject, UID predicate, NODE object, UID context) {
        try {
            removeMatch(subject, predicate, object, context);
        } catch (SQLException e) {
            throw new RepositoryException(e);
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
                if (bulkLoadDir != null && addedStatements.size() > 1000){
                    addBulk(addedStatements);
                }else{
                    add(addedStatements);    
                }                
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

    public Connection getConnection(){
        return connection;
    }

}
