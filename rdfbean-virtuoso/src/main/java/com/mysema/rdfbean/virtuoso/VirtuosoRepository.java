package com.mysema.rdfbean.virtuoso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

/**
 * @author tiwe
 *
 */
public class VirtuosoRepository implements Repository {

    private static final Logger logger = LoggerFactory.getLogger(VirtuosoRepository.class);

    private static final int LOAD_BATCH_SIZE = 2000;

    private static RDFFormat getRioFormat(Format format){
        switch(format){
            case N3: return RDFFormat.N3;
            case NTRIPLES: return RDFFormat.NTRIPLES;
            case RDFA: return RDFFormat.RDFA;
            case RDFXML: return RDFFormat.RDFXML;
            case TRIG: return RDFFormat.TRIG;
            case TURTLE: return RDFFormat.TURTLE;
        }
        throw new IllegalArgumentException("Unsupported format : " + format);
    }

    private final VirtuosoConnectionPoolDataSource pds = new VirtuosoConnectionPoolDataSource();

    private final String host;
    
    private final Converter converter = new Converter(new ConverterRegistryImpl());

    private RDFSource[] sources;

    private File dataDir;
    
    private File bulkLoadDir;

    private final String charset = "UTF-8";

    private final UID defGraph;
    
    private Collection<UID> allowedGraphs = Collections.emptySet();

    private int prefetchSize = 200;

    private boolean initialized = false;

    public VirtuosoRepository(String hostlist, String user, String password) {
        this(hostlist, user, password, "rdfbean:nil");
    }
    
    public VirtuosoRepository(String host, int port, String user, String password) {
        this(host, port, user, password, "rdfbean:nil");
    }

    public VirtuosoRepository(String hostlist, String user, String password, String defGraph) {
        this(hostlist, 1111, user, password, defGraph);
    }
    
    public VirtuosoRepository(String host, int port, String user, String password, String defGraph) {
        this.defGraph = new UID(defGraph);
        this.host = host;
        pds.setServerName(host);
        pds.setPortNumber(port);
        pds.setUser(user);
        pds.setPassword(password);
        pds.setCharset(charset);
    }

    @Override
    public void close() {
        initialized = false;
    }

    @Override
    public <RT> RT execute(Operation<RT> operation) {
        RDFConnection connection = openConnection();
        try{
            try{
                RDFBeanTransaction tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
                try{
                    RT retVal = operation.execute(connection);
                    tx.commit();
                    return retVal;
                }catch(IOException io){
                    tx.rollback();
                    throw io;
                }
            }finally{
                connection.close();
            }
        }catch(IOException io){
            throw new RepositoryException(io);
        }
    }

    @Override
    public void export(Format format, Map<String, String> ns2prefix, OutputStream out) {
        RDFFormat targetFormat = getRioFormat(format);
        RDFWriter writer = Rio.createWriter(targetFormat, out);
        try {
            RDFConnection conn = openConnection();
            try{
                writer.startRDF();
                for (Map.Entry<String, String> entry : ns2prefix.entrySet()){
                    writer.handleNamespace(entry.getValue(), entry.getKey());
                }
                CloseableIterator<STMT> stmts = conn.findStatements(null, null, null, null, false);
                ValueFactory valueFactory = new ValueFactoryImpl();
                SesameDialect dialect = new SesameDialect(valueFactory);
                try{
                    while (stmts.hasNext()){
                        STMT stmt = stmts.next();
                        Resource sub = dialect.getResource(stmt.getSubject());
                        URI pre = dialect.getURI(stmt.getPredicate());
                        Value obj = dialect.getNode(stmt.getObject());
                        writer.handleStatement(valueFactory.createStatement(sub, pre, obj));
                    }
                }finally{
                    stmts.close();
                }
                writer.endRDF();
            }finally{
                conn.close();
            }
        } catch (RDFHandlerException e) {
            throw new RepositoryException(e.getMessage(), e);
        }      }

    @Override
    public void export(Format format, OutputStream out) {
        export(format, Namespaces.DEFAULT, out);
    }

    public VirtuosoRepositoryConnection openConnection() {
        try {
            javax.sql.PooledConnection pconn = pds.getPooledConnection();
            java.sql.Connection connection = pconn.getConnection();
            return new VirtuosoRepositoryConnection(converter, prefetchSize, defGraph, allowedGraphs, connection, bulkLoadDir);
        } catch (SQLException e) {
            logger.error("Connection to " + host + " FAILED.");
            throw new RepositoryException(e);
        }
    }

    public File getDataDir() {
        return this.dataDir;
    }

    public int getFetchSize() {
        return this.prefetchSize;
    }
    
    public void initialize() {
        RDFConnection connection = openConnection();
        RDFBeanTransaction tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
        try {
            if (sources != null) {
                for (RDFSource source : sources) {
                    if (source.getResource() != null){
                        logger.info("loading " + source.getResource());
                    }
                    load(source.getFormat(), source.openStream(), new UID(source.getContext()), false);
                }
            }
            tx.commit();
        } catch(Exception e){
            tx.rollback();
            throw new RepositoryException(e);
        } finally {
            connection.close();
        }
        initialized = true;
    }
    
    @Override
    public void load(Format format, InputStream is, UID context, boolean replace) {
        ValueFactory valueFactory = new ValueFactoryImpl();
        SesameDialect dialect = new SesameDialect(valueFactory);
        RDFConnection connection = openConnection();
        try{
            if (!replace && context != null){
                if (connection.exists(null, null, null, context, false)){
                    return;
                }
            }
            if (context != null && replace){
                connection.remove(null, null, null, context);
            }
            Set<STMT> stmts = new HashSet<STMT>(LOAD_BATCH_SIZE);
            RDFParser parser = Rio.createParser(getRioFormat(format));
            parser.setRDFHandler(createHandler(dialect, connection, stmts, context));
            parser.parse(is, context != null ? context.getValue() : TEST.NS);
            connection.update(Collections.<STMT>emptySet(), stmts);
        } catch (RDFParseException e) {
            throw new RepositoryException(e);
        } catch (RDFHandlerException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }finally{
            connection.close();
        }
    }

    private RDFHandler createHandler(
            final SesameDialect dialect,
            final RDFConnection connection, final Set<STMT> stmts, @Nullable final UID context) {
        return new RDFHandlerBase(){
            @Override
            public void handleStatement(Statement stmt) throws RDFHandlerException {
                ID sub = dialect.getID(stmt.getSubject());
                UID pre = dialect.getUID(stmt.getPredicate());
                NODE obj = dialect.getNODE(stmt.getObject());
                stmts.add(new STMT(sub, pre, obj, context));

                if (stmts.size() == LOAD_BATCH_SIZE){
                    connection.update(Collections.<STMT>emptySet(), stmts);
                    stmts.clear();
                }
            }
        };
    }

    public void setDataDir(File dataDir) {
        this.dataDir = Assert.notNull(dataDir,"dataDir");
    }
    
    public void setBulkLoadDir(File bulkLoadDir) {
        this.bulkLoadDir = Assert.notNull(bulkLoadDir,"bulkLoadDir");
    }

    public void setFetchSize(int sz) {
        this.prefetchSize = sz;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setSources(RDFSource... sources) {
        this.sources = sources;
    }

    public void setAllowedGraphs(Collection<UID> allowedGraphs) {
        this.allowedGraphs = Assert.notNull(allowedGraphs,"allowedGraphs");
    }
    
}
