package com.mysema.rdfbean.virtuoso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.Namespaces;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.model.io.RDFWriter;
import com.mysema.rdfbean.model.io.WriterUtils;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

/**
 * @author tiwe
 *
 */
public class VirtuosoRepository implements Repository {

    private static final Logger logger = LoggerFactory.getLogger(VirtuosoRepository.class);

    private final VirtuosoConnectionPoolDataSource pds = new VirtuosoConnectionPoolDataSource();

    private final String host;
    
    private final Converter converter = new Converter(new ConverterRegistryImpl());

    private RDFSource[] sources;

    private File dataDir;
    
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
        RDFWriter writer = WriterUtils.createWriter(format, out, ns2prefix);
        RDFConnection conn = openConnection();
        try{                
            CloseableIterator<STMT> stmts = conn.findStatements(null, null, null, null, false);
            try{
                writer.begin();
                while (stmts.hasNext()){
                    writer.handle(stmts.next());
                }
                writer.end();
            }finally{
                stmts.close();
            }
            
        }finally{
            conn.close();
        }    
    }

    @Override
    public void export(Format format, OutputStream out) {
        export(format, Namespaces.DEFAULT, out);
    }

    public VirtuosoRepositoryConnection openConnection() {
        try {
            javax.sql.PooledConnection pconn = pds.getPooledConnection();
            java.sql.Connection connection = pconn.getConnection();
            return new VirtuosoRepositoryConnection(converter, prefetchSize, defGraph, allowedGraphs, connection);
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
        VirtuosoRepositoryConnection connection = openConnection();
        RDFBeanTransaction tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
        try {
            if (sources != null) {
                for (RDFSource source : sources) {
                    if (source.getResource() != null){
                        logger.info("loading " + source.getResource());
                    }
                    UID context = new UID(source.getContext());
                    InputStream is = source.openStream();
                    try{
                        connection.load(source.getFormat(), is, context, false);    
                    }finally{
                        is.close();
                    }                    
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
        VirtuosoRepositoryConnection connection = openConnection();
        try{
            try{
                connection.load(format, is, context, replace);    
            }finally{
                is.close();
            }            
        } catch (SQLException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }finally{
            connection.close();
        }
    }

    public void setDataDir(File dataDir) {
        this.dataDir = Assert.notNull(dataDir,"dataDir");
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
